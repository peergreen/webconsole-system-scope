package com.peergreen.webconsole.scope.system.internal.bundle;

import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.ISecurityManager;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Ready;
import com.peergreen.webconsole.scope.system.SystemTab;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.system.internal.SystemScope.tab")
@SystemTab("OSGi Bundles")
public class BundleViewer extends VerticalLayout {

    @Inject
    private BundleContext bundleContext;
    @Inject
    private ISecurityManager securityManager;
    private Table table;

    @Ready
    public void createView() {
        setMargin(true);
        setSpacing(true);

        table = new Table();
        table.addContainerProperty("Bundle Symbolic Name", String.class, null);
        table.addContainerProperty("Version", String.class, null);
        table.addContainerProperty("State", String.class, null);
        table.addContainerProperty("Active", CheckBox.class, null);
        table.setSizeFull();
        table.setSortContainerPropertyId("Bundle Symbolic Name");
        table.setSortAscending(true);
        table.setImmediate(true);

        refreshTable();
        addComponent(table);
    }

    private void refreshTable() {
        Bundle[] bundles = bundleContext.getBundles();
        table.removeAllItems();

        int i = 1;
        for (Bundle bundle : bundles) {
            final Bundle selectedBundle = bundle;
            CheckBox checkBox = new CheckBox();
            if (!securityManager.isUserInRole("superadmin")) checkBox.setEnabled(false);
            checkBox.setImmediate(true);
            checkBox.setValue(bundle.getState() == Bundle.ACTIVE);
            checkBox.addValueChangeListener(new ValueChangeListener() {

                @Override
                public void valueChange(
                        com.vaadin.data.Property.ValueChangeEvent event)
                {
                    if (selectedBundle.getState() == Bundle.ACTIVE) {
                        try {
                            selectedBundle.stop();
                            refreshTable();
                        } catch (BundleException e1) {
                            e1.printStackTrace();
                        }
                    } else if (selectedBundle.getState() == Bundle.RESOLVED) {
                        try {
                            selectedBundle.start();
                            refreshTable();
                        } catch (BundleException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
            table.addItem(
                    new Object[] {
                            bundle.getSymbolicName(),
                            bundle.getVersion().toString(),
                            getStateString(bundle),
                            checkBox
                    },
                    i++);
        }
        table.sort();
    }

    String getStateString(Bundle bundle) {
        switch (bundle.getState()) {
            case Bundle.ACTIVE:
                return "ACTIVE";
            case Bundle.INSTALLED:
                return "INSTALLED";
            case Bundle.RESOLVED:
                return "RESOLVED";
            case Bundle.UNINSTALLED:
                return "UNINSTALLED";
            default:
                return "UNKNOWN";
        }
    }
}
