package com.cloudbees.jenkins.plugins.amazonecs;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.apache.commons.lang.builder.EqualsBuilder;

public class ECSTaskTemplateTest {

    @Test
    public void shouldCombine() throws Exception {

        ECSTaskTemplate child = new ECSTaskTemplate(
            "child-name", "child-label",
            null, "child-image", "EC2", "child-remoteFSRoot",
            0, 0, 0, null, null, false, false,
            "child-containerUser", null, null, null, null, null, null, "parent");

        ECSTaskTemplate parent = new ECSTaskTemplate(
            "parent-name", "parent-label",
            null, "parent-image", "FARGATE", "parent-remoteFSRoot",
            0, 0, 0, null, null, false, false,
            "parent-containerUser", null, null, null, null, null, null, null);

        ECSTaskTemplate expected = new ECSTaskTemplate(
            "child-name", "child-label",
            null, "child-image", "EC2", "child-remoteFSRoot",
            0, 0, 0, null, null, false, false,
            "child-containerUser", null, null, null, null, null, null, null);


        ECSTaskTemplate result = child.combine(parent);

        assertTrue(EqualsBuilder.reflectionEquals(expected, result));
    }

    @Test
    public void shouldReturnSettingsFromParent() throws Exception {

        ECSTaskTemplate child = new ECSTaskTemplate(
            "child-name", "child-label",
            null, null, "EC2", "child-remoteFSRoot", // image is set to null
            0, 0, 0, null, null, false, false,
            "child-containerUser", null, null, null, null, null, null, "parent");

        ECSTaskTemplate parent = new ECSTaskTemplate(
            "parent-name", "parent-label",
            null, "parent-image", "FARGATE", "parent-remoteFSRoot",
            0, 0, 0, null, null, false, false,
            "parent-containerUser", null, null, null, null, null, null, null);

        ECSTaskTemplate expected = new ECSTaskTemplate(
            "child-name", "child-label",
            null, "parent-image", "EC2", "child-remoteFSRoot",
            0, 0, 0, null, null, false, false,
            "child-containerUser", null, null, null, null, null, null, null);

        ECSTaskTemplate result = child.combine(parent);

        assertTrue(EqualsBuilder.reflectionEquals(expected, result));
    }

    @Test
    public void shouldReturnChildIfNoParent() throws Exception {

        ECSTaskTemplate child = new ECSTaskTemplate(
            "child-name", "child-label",
            null, "child-image", "EC2", "child-remoteFSRoot",
            0, 0, 0, null, null, false, false,
            "child-containerUser", null, null, null, null, null, null, null); // inheritFrom is null

        ECSTaskTemplate expected = new ECSTaskTemplate(
            "child-name", "child-label",
            null, "child-image", "EC2", "child-remoteFSRoot",
            0, 0, 0, null, null, false, false,
            "child-containerUser", null, null, null, null, null, null, null);

        ECSTaskTemplate result = child.combine(null);

        assertTrue(EqualsBuilder.reflectionEquals(expected, result));
    }
}
