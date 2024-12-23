package com.zj.client.service;

import com.zj.client.entity.dto.MavenConfigDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
public class MavenSettingHelper {

    public static final String WINDY_AUTO_CONFIG_ID = "windy-auto-config";

    public static void main(String[] args) {
        MavenConfigDto mavenConfigDto = new MavenConfigDto();
        mavenConfigDto.setMavenPath("/Users/falcon/docker/client/maven");
        MavenConfigDto.RemoteRepository repository = new MavenConfigDto.RemoteRepository();
        repository.setRepositoryId("xxxxx");
        repository.setRepositoryUrl("http://xxxxx1");
        List<MavenConfigDto.RemoteRepository> remoteRepositories = new ArrayList<>();
        remoteRepositories.add(repository);

        MavenConfigDto.RemoteRepository repository1 = new MavenConfigDto.RemoteRepository();
        repository1.setRepositoryId("yyyyyyy");
        repository1.setRepositoryUrl("http://yyyyyy");
        repository1.setUserName("admin");
        repository1.setPassword("admin");
        remoteRepositories.add(repository1);
        mavenConfigDto.setRemoteRepositories(remoteRepositories);
        MavenSettingHelper.configSetting(mavenConfigDto);
    }
    public static boolean configSetting(MavenConfigDto mavenConfigDto) {
        try {
            String settingPath = mavenConfigDto.getMavenPath() + "/conf/settings.xml";
            File settingsFile = new File(settingPath);
            if (!settingsFile.exists()) {
                log.info("settings file does not exist={}", settingPath);
                return false;
            }
            log.info("start config maven setting path={}", settingPath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(settingsFile);

            // 更新 profile 节点
            updateProfileNode(document, mavenConfigDto.getRemoteRepositories());

            // 更新servers
            updateOrAddServers(document, mavenConfigDto.getRemoteRepositories());

            updateActiveProfiles(document);
            removeWhitespaceNodes(document.getDocumentElement());

            // 保存更新后的配置
            saveXMLDocument(document, settingsFile);
            log.info("update maven config success");
            return true;
        } catch (Exception e) {
           log.info("config maven config error", e);
        }
        return false;
    }

    private static void updateActiveProfiles(Document document) {
        NodeList activeProfilesList = document.getElementsByTagName("activeProfiles");
        Element activeProfiles;

        // 检查是否已有 activeProfiles 节点
        if (activeProfilesList.getLength() > 0) {
            activeProfiles = (Element) activeProfilesList.item(0);
        } else {
            // 如果不存在则创建 activeProfiles 节点
            activeProfiles = document.createElement("activeProfiles");
            document.getDocumentElement().appendChild(activeProfiles);
        }

        // 检查是否已有目标 activeProfile
        NodeList activeProfileNodes = activeProfiles.getElementsByTagName("activeProfile");
        for (int i = 0; i < activeProfileNodes.getLength(); i++) {
            if (activeProfileNodes.item(i).getTextContent().equals(WINDY_AUTO_CONFIG_ID)) {
                return; // 如果已经存在，直接返回
            }
        }

        // 创建新的 activeProfile 节点
        Element activeProfile = document.createElement("activeProfile");
        activeProfile.setTextContent(WINDY_AUTO_CONFIG_ID);
        activeProfiles.appendChild(activeProfile);
    }

    private static void removeWhitespaceNodes(Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = childNodes.getLength() - 1; i >= 0; i--) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.TEXT_NODE && child.getTextContent().trim().isEmpty()) {
                node.removeChild(child); // 移除空白文本节点
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                removeWhitespaceNodes(child); // 递归处理子节点
            }
        }
    }

    private static void updateProfileNode(Document document, List<MavenConfigDto.RemoteRepository> repositories) {
        NodeList profilesList = document.getElementsByTagName("profile");
        Element targetProfile = null;

        // 查找匹配的 profile 节点
        for (int i = 0; i < profilesList.getLength(); i++) {
            Element profile = (Element) profilesList.item(i);
            NodeList idNodes = profile.getElementsByTagName("id");
            if (idNodes.getLength() > 0 && Objects.equals(WINDY_AUTO_CONFIG_ID, idNodes.item(0).getTextContent())) {
                targetProfile = profile;
                break;
            }
        }

        // 如果未找到 profile，则创建一个新的
        if (Objects.isNull(targetProfile)) {
            targetProfile = createProfileNode(document);
        }

        // 更新或新增 remoteRepository 节点
        for (MavenConfigDto.RemoteRepository repo : repositories) {
            updateOrAddRemoteRepository(document, targetProfile, repo);
        }
    }

    // 创建新的 profile 节点
    private static Element createProfileNode(Document document) {
        Element profiles = getOrCreateProfilesNode(document);

        Element profile = document.createElement("profile");
        Element idElement = document.createElement("id");
        idElement.setTextContent(WINDY_AUTO_CONFIG_ID);

        profile.appendChild(idElement);
        profiles.appendChild(profile);
        return profile;
    }

    // 确保 profiles 节点存在
    private static Element getOrCreateProfilesNode(Document document) {
        NodeList profilesList = document.getElementsByTagName("profiles");
        if (profilesList.getLength() > 0) {
            return (Element) profilesList.item(0);
        } else {
            Element profiles = document.createElement("profiles");
            document.getDocumentElement().appendChild(profiles);
            return profiles;
        }
    }

    // 更新或新增单个 remoteRepository 节点
    private static void updateOrAddRemoteRepository(Document document, Element profile,
                                                    MavenConfigDto.RemoteRepository repo) {
        NodeList repositoryList = profile.getElementsByTagName("repository");
        Element targetRemoteRepository = null;

        // 查找匹配的 remoteRepository 节点
        for (int i = 0; i < repositoryList.getLength(); i++) {
            Element remoteRepository = (Element) repositoryList.item(i);
            NodeList idNodes = remoteRepository.getElementsByTagName("id");
            if (idNodes.getLength() > 0 && repo.getRepositoryId().equals(idNodes.item(0).getTextContent())) {
                targetRemoteRepository = remoteRepository;
                break;
            }
        }

        // 如果未找到 remoteRepository，则创建一个新的
        if (targetRemoteRepository == null) {
            targetRemoteRepository = createRemoteRepositoryNode(document, repo);
            profile.appendChild(targetRemoteRepository);
        } else {
            // 更新已有 remoteRepository
            updateRemoteRepositoryNode(targetRemoteRepository, repo);
        }
    }

    // 创建新的 remoteRepository 节点
    private static Element createRemoteRepositoryNode(Document document, MavenConfigDto.RemoteRepository repo) {
        Element remoteRepository = document.createElement("repository");

        Element idElement = document.createElement("id");
        idElement.setTextContent(repo.getRepositoryId());
        Element urlElement = document.createElement("url");
        urlElement.setTextContent(repo.getRepositoryUrl());

        remoteRepository.appendChild(idElement);
        remoteRepository.appendChild(urlElement);
        return remoteRepository;
    }

    // 更新已有 remoteRepository 节点
    private static void updateRemoteRepositoryNode(Element remoteRepository, MavenConfigDto.RemoteRepository repo) {
        NodeList children = remoteRepository.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            if ("url".equals(child.getNodeName())) {
                child.setTextContent(repo.getRepositoryUrl());
            }
        }
    }

    private static void updateOrAddServers(Document document, List<MavenConfigDto.RemoteRepository> remoteRepositories) {
        if (CollectionUtils.isEmpty(remoteRepositories)) {
            return;
        }
        NodeList serversList = document.getElementsByTagName("server");
        for (MavenConfigDto.RemoteRepository repository : remoteRepositories) {
            boolean found = false;
            for (int i = 0; i < serversList.getLength(); i++) {
                Element server = (Element) serversList.item(i);
                if (server.getElementsByTagName("id").item(0).getTextContent().equals(repository.getRepositoryId())) {
                    updateServerNode(server, repository.getUserName(), repository.getPassword());
                    found = true;
                    break;
                }
            }
            if (!found) {
                NodeList serversContainer = document.getElementsByTagName("servers");
                Element servers = (serversContainer.getLength() > 0)
                        ? (Element) serversContainer.item(0)
                        : createServersNode(document);

                Element newServer = createServerNode(document, repository);
                servers.appendChild(newServer);
            }
        }
    }

    private static void updateServerNode(Element server, String username, String password) {
        NodeList children = server.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            if ("username".equals(child.getNodeName())) {
                child.setTextContent(username);
            } else if ("password".equals(child.getNodeName())) {
                child.setTextContent(password);
            }
        }
    }

    private static Element createServerNode(Document document, MavenConfigDto.RemoteRepository repository) {
        Element server = document.createElement("server");
        Element idElement = document.createElement("id");
        idElement.setTextContent(repository.getRepositoryId());
        Element usernameElement = document.createElement("username");
        usernameElement.setTextContent(repository.getUserName());
        Element passwordElement = document.createElement("password");
        passwordElement.setTextContent(repository.getPassword());

        server.appendChild(idElement);
        server.appendChild(usernameElement);
        server.appendChild(passwordElement);
        return server;
    }

    private static Element createServersNode(Document document) {
        Element servers = document.createElement("servers");
        document.getDocumentElement().appendChild(servers);
        return servers;
    }

    private static void saveXMLDocument(Document document, File file) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(file);

        transformer.transform(source, result);
    }
}
