package com.zj.client.service;

import com.zj.client.entity.dto.MavenConfigDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

@Slf4j
public class MavenSettingHelper {
    private static final String SERVER_ID = "windy-config-maven";

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

            //更新mirror
            updateOrAddMirror(document, mavenConfigDto.getRepositoryUrl());

            //更新server
            updateOrAddServer(document, mavenConfigDto.getUserName(), mavenConfigDto.getPassword());

            // 保存更新后的配置
            saveXMLDocument(document, settingsFile);
            log.info("update maven config success");
            return true;
        } catch (Exception e) {
           log.info("config maven config error", e);
        }
        return false;
    }

    private static void updateOrAddMirror(Document document, String url) {
        String mirrorOf = "central";
        NodeList mirrorsList = document.getElementsByTagName("mirror");
        boolean found = false;

        for (int i = 0; i < mirrorsList.getLength(); i++) {
            Element mirror = (Element) mirrorsList.item(i);
            if (mirror.getElementsByTagName("id").item(0).getTextContent().equals(SERVER_ID)) {
                updateMirrorNode(mirror, url, mirrorOf);
                found = true;
                break;
            }
        }

        if (!found) {
            NodeList mirrorsContainer = document.getElementsByTagName("mirrors");
            Element mirrors = (mirrorsContainer.getLength() > 0)
                    ? (Element) mirrorsContainer.item(0)
                    : createMirrorsNode(document);

            Element newMirror = createMirrorNode(document, url, mirrorOf);
            mirrors.appendChild(newMirror);
        }
    }

    private static void updateOrAddServer(Document document, String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            log.info("maven sever username or password is empty, not config");
            return;
        }
        NodeList serversList = document.getElementsByTagName("server");
        boolean found = false;

        for (int i = 0; i < serversList.getLength(); i++) {
            Element server = (Element) serversList.item(i);
            if (server.getElementsByTagName("id").item(0).getTextContent().equals(SERVER_ID)) {
                updateServerNode(server, username, password);
                found = true;
                break;
            }
        }

        if (!found) {
            NodeList serversContainer = document.getElementsByTagName("servers");
            Element servers = (serversContainer.getLength() > 0)
                    ? (Element) serversContainer.item(0)
                    : createServersNode(document);

            Element newServer = createServerNode(document, SERVER_ID, username, password);
            servers.appendChild(newServer);
        }
    }

    private static Element createMirrorNode(Document document, String url, String mirrorOf) {
        Element mirror = document.createElement("mirror");
        Element idElement = document.createElement("id");
        idElement.setTextContent(SERVER_ID);

        Element nameElement = document.createElement("name");
        nameElement.setTextContent("Windy auto config");
        Element urlElement = document.createElement("url");
        urlElement.setTextContent(url);
        Element mirrorOfElement = document.createElement("mirrorOf");
        mirrorOfElement.setTextContent(mirrorOf);

        mirror.appendChild(idElement);
        mirror.appendChild(nameElement);
        mirror.appendChild(urlElement);
        mirror.appendChild(mirrorOfElement);
        return mirror;
    }

    private static void updateMirrorNode(Element mirror, String url, String mirrorOf) {
        NodeList children = mirror.getChildNodes();
        for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            if ("url".equals(child.getNodeName())) {
                child.setTextContent(url);
            } else if ("mirrorOf".equals(child.getNodeName())) {
                child.setTextContent(mirrorOf);
            }
        }
    }

    private static Element createServerNode(Document document, String id, String username, String password) {
        Element server = document.createElement("server");
        Element idElement = document.createElement("id");
        idElement.setTextContent(id);
        Element usernameElement = document.createElement("username");
        usernameElement.setTextContent(username);
        Element passwordElement = document.createElement("password");
        passwordElement.setTextContent(password);

        server.appendChild(idElement);
        server.appendChild(usernameElement);
        server.appendChild(passwordElement);

        return server;
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

    private static Element createMirrorsNode(Document document) {
        Element mirrors = document.createElement("mirrors");
        document.getDocumentElement().appendChild(mirrors);
        return mirrors;
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
