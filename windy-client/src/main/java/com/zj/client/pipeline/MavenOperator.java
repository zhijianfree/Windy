package com.zj.client.pipeline;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author falcon
 * @since 2023/3/29
 */
@Component
public class MavenOperator {

  @Value("${windy.pipeline.maven.path}")
  private String mavenDir;

  public Integer build(String pomPath) throws MavenInvocationException {
    InvocationRequest ideaRequest = new DefaultInvocationRequest();
    ideaRequest.setBaseDirectory(new File(pomPath));
    ideaRequest.setAlsoMakeDependents(true);
    ideaRequest.setGoals(Arrays.asList("clean","package"));

    Invoker ideaInvoker = new DefaultInvoker();
    ideaInvoker.setMavenHome(new File(mavenDir));
    ideaInvoker.setOutputHandler(System.out::println);
    InvocationResult ideaResult = ideaInvoker.execute(ideaRequest);
    return ideaResult.getExitCode();
  }
}
