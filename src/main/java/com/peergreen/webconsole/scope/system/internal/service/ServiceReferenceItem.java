package com.peergreen.webconsole.scope.system.internal.service;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

/**
 * User: guillaume
 * Date: 17/07/13
 * Time: 11:32
 */
public class ServiceReferenceItem {
    private boolean opened = false;
    private final ServiceReference<?> reference;

    public ServiceReferenceItem(final ServiceReference<?> reference) {
        this.reference = reference;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(final boolean opened) {
        this.opened = opened;
    }

    public Long getServiceId() {
        return (Long) reference.getProperty(Constants.SERVICE_ID);
    }

    public String getBundleInfo() {
        Bundle bundle = reference.getBundle();
        return format("%s (%d)", bundle.getSymbolicName(), bundle.getBundleId());
    }

    public Label getInterfaces() {
        if (!opened) {
            return new Label(searchInterfaces());
        } else {
            StringBuilder sb = new StringBuilder();

            for (String name : reference.getPropertyKeys()) {
                printServicePropery(sb, name, reference.getProperty(name));
            }

            return new Label(sb.toString(), ContentMode.HTML);
        }
    }

    private void printServicePropery(final StringBuilder sb, final String name, final Object value) {
        sb.append("<b>");
        sb.append(name);
        sb.append("</b>");
        sb.append(" ");
        sb.append(convert(value));
        sb.append("<br/>");
    }

    private Object convert(final Object value) {
        if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            return Arrays.asList(array);
        }
        return value;
    }

    private String searchInterfaces() {
        List<String> interfaces = new ArrayList<>();
        String[] classes = (String[]) reference.getProperty(Constants.OBJECTCLASS);
        interfaces.addAll(Arrays.asList(classes));
        return interfaces.toString();
    }
}
