package com.peergreen.webconsole.scope.system.extensions;

import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Ready;
import com.peergreen.webconsole.scope.system.SystemTab;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.system.SystemScope.tab")
@SystemTab("OSGi Services")
public class ServiceViewer extends VerticalLayout {

    @Inject
    private BundleContext bundleContext;
    private Table table;

    @Ready
    public void createView() {
        setMargin(true);
        setSpacing(true);

        table = new Table();

        table.addContainerProperty("Service ID", Long.class, null);
        table.addContainerProperty("Interfaces", String.class, null);
        table.addContainerProperty("Bundle", String.class, null);

        table.setSizeFull();
        table.setSortContainerPropertyId("service-id");
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
            if (bundle.getRegisteredServices() != null) {
                for (ServiceReference<?> reference : bundle.getRegisteredServices()) {
                    table.addItem(
                            new Object[] {
                                    reference.getProperty(Constants.SERVICE_ID),
                                    getInterfaces(reference),
                                    format("%s (%d)", bundle.getSymbolicName(), bundle.getBundleId())
                            }, i++);
                }
            }
        }
        table.sort();
    }

    String getInterfaces(ServiceReference<?> reference) {
        List<String> interfaces = new ArrayList<>();
        String[] classes = (String[]) reference.getProperty(Constants.OBJECTCLASS);
        interfaces.addAll(Arrays.asList(classes));
        return interfaces.toString();
    }

}
