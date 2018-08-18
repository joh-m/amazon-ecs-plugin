package com.cloudbees.jenkins.plugins.amazonecs.pipeline;

import hudson.Extension;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import java.io.Serializable;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import hudson.model.Run;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ECSTaskTemplateStep extends Step implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ECSTaskTemplateStep.class.getName());

    private String DEFAULT_CLOUD = "a";
    private String cloud = DEFAULT_CLOUD;
    private final String label;
    private final String name;
    private int memory;
    private int cpu;
    private String image;
    private String taskRoleArn;

    @DataBoundConstructor
    public ECSTaskTemplateStep(String label, String cloud, int memory, int cpu, String name, String image, String taskRoleArn) {
        this.cloud = cloud;
        this.label = label;
        this.name = name == null ? "jenkins-slave" : name;
        this.memory = memory;
        this.cpu = cpu;
        this.image = image;
        this.taskRoleArn = taskRoleArn;
    }

    public String getCloud() {
        return cloud;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public int getMemory() {
        return memory;
    }

    public int getCpu() {
        return cpu;
    }

    public String getImage() {
        return image;
    }

    public String getTaskRoleArn() {
        return taskRoleArn;
    }


    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        LOGGER.log(Level.INFO, "In ECSTaskTemplateStep start. label: {0}", label);
        LOGGER.log(Level.INFO, "In ECSTaskTemplateStep start. cloud: {0}", cloud);
        return new ECSTaskTemplateStepExecution(this, stepContext);
    }

    @Extension(optional = true)
    public static final class DescriptorImpl extends StepDescriptor {
        @Override
        public String getFunctionName() {
            return "taskTemplate";
        }
        @Override
        public String getDisplayName() {
            return "Cloud instances provisioning for declarative pipeline";
        }

        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }

        @Override
        public boolean isAdvanced() {
            return true;
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class);
        }
    }

    @Override
    public String toString() {
        return "Step options: " +
                "ecs{" + '\n' +
                "cloud='" + cloud + '\'' + '\n' +
                '}';
    }
}
