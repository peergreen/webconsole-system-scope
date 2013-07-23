package com.peergreen.webconsole.scope.system.internal.bundle.actions;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import com.peergreen.webconsole.INotifierService;
import com.vaadin.ui.Button;

/**
* User: guillaume
* Date: 22/07/13
* Time: 13:18
*/
public class StopBundleClickListener implements Button.ClickListener {

    private final Bundle bundle;
    private final INotifierService notifierService;

    public StopBundleClickListener(final Bundle bundle, final INotifierService notifierService) {
        this.bundle = bundle;
        this.notifierService = notifierService;
    }

    @Override
    public void buttonClick(final Button.ClickEvent event) {
        try {
            bundle.stop();
        } catch (BundleException e) {
            notifierService.addNotification(e.getMessage());
        }
    }
}
