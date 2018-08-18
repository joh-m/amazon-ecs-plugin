package com.cloudbees.jenkins.plugins.amazonecs.pipeline;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.pipeline.modeldefinition.agent.DeclarativeAgent;
import org.jenkinsci.plugins.pipeline.modeldefinition.agent.DeclarativeAgentDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.jenkinsci.plugins.variant.OptionalExtension;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.Map;
import java.util.TreeMap;

public class ECSDeclarativeAgent extends DeclarativeAgent<ECSDeclarativeAgent> {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ECSDeclarativeAgent.class.getName());

    private String label;
    private String cloud;
    private int memory;
    private int cpu;
    private String image;
    private String taskRoleArn;

    @DataBoundConstructor
    public ECSDeclarativeAgent() {
    }

    public String getLabel() {
        return label;
    }

    @DataBoundSetter
    public void setLabel(String label) {
        this.label = label;
    }

    public String getCloud() {
        return cloud;
    }

    @DataBoundSetter
    public void setCloud(String cloud) {
        this.cloud = cloud;
    }

    @DataBoundSetter
    public void setMemory(int memory) {
        this.memory = memory;
    }

    @DataBoundSetter
    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    @DataBoundSetter
    public void setImage(String image) {
        this.image = image;
    }

    @DataBoundSetter
    public void setTaskRoleArn(String taskRoleArn) {
        this.taskRoleArn = taskRoleArn;
    }


    public Map<String,Object> getAsArgs() {
        Map<String,Object> argMap = new TreeMap<String, Object>();

        LOGGER.log(Level.INFO, "In getAsArgs. label: {0}", label);

        argMap.put("label", label);
        argMap.put("name", label);

        if (!StringUtils.isEmpty(cloud)) {
            argMap.put("cloud", cloud);
        }

        if (memory != 0) {
            argMap.put("memory", memory);
        }

        if (cpu != 0) {
            argMap.put("cpu", cpu);
        }

        if (!StringUtils.isEmpty(image)) {
            argMap.put("image", image);
        }

        if (!StringUtils.isEmpty(taskRoleArn)) {
            argMap.put("taskRoleArn", taskRoleArn);
        }

        LOGGER.log(Level.INFO, "In getAsArgs. argMap: {0}", argMap.toString());

        return argMap;
    }

    @OptionalExtension(requirePlugins = "pipeline-model-extensions")
    @Symbol("ecs")
    public static class DescriptorImpl extends DeclarativeAgentDescriptor<ECSDeclarativeAgent> {
      @Override
      public String getDisplayName() {
          return "ECS Agent";
      }
    }
}
