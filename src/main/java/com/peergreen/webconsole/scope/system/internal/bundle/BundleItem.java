package com.peergreen.webconsole.scope.system.internal.bundle;

import static java.lang.String.format;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;

import com.sun.media.sound.DataPusher;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

/**
 * User: guillaume
 * Date: 18/07/13
 * Time: 11:28
 */
public class BundleItem {
    private final Bundle bundle;

    public BundleItem(final Bundle bundle) {
        this.bundle = bundle;
    }

    public long getBundleId() {
        return bundle.getBundleId();
    }

    public Label getBundleName() {
        String name = find(Constants.BUNDLE_NAME);
        if (name != null) {
            return new Label(format("%s <i>(%s)</i>", name, bundle.getSymbolicName()),
                             ContentMode.HTML);
        }
        return new Label(bundle.getSymbolicName());
    }

    private String find(final String name) {
        return bundle.getHeaders().get(name);
    }

    public String getVersion() {
        return bundle.getVersion().toString();
    }

    public int getState() {
        return bundle.getState();
    }

    public Bundle getBundle() {
        return bundle;
    }
}
