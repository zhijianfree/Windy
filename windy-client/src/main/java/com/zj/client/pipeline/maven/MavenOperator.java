package com.zj.client.pipeline.maven;

import com.google.common.base.Preconditions;
import java.io.File;
import java.util.Arrays;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author guyuelan
 * @since 2023/3/29
 */
@Component
public class MavenOperator {

  public static final String MAVEN_PATH_KEY = "windy.pipeline.maven.path";
  private final Environment environment;

  public MavenOperator(Environment environment) {
    this.environment = environment;
  }

  public Integer build(String pomPath) throws Exception {
    InvocationRequest ideaRequest = new DefaultInvocationRequest();
    ideaRequest.setBaseDirectory(new File(pomPath));
    ideaRequest.setAlsoMakeDependents(true);
    ideaRequest.setGoals(Arrays.asList("clean","package"));

    String mavenDir = environment.getProperty(MAVEN_PATH_KEY);
    Preconditions.checkNotNull(mavenDir,"maven path can not find , consider to fix it");
    Invoker ideaInvoker = new DefaultInvoker();
    ideaInvoker.setMavenHome(new File(mavenDir));
    ideaInvoker.setOutputHandler(System.out::println);
    InvocationResult ideaResult = ideaInvoker.execute(ideaRequest);
    return ideaResult.getExitCode();
  }
}
