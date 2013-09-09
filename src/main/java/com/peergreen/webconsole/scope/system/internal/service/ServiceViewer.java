package com.peergreen.webconsole.scope.system.internal.service;

import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.navigator.Navigable;
import com.peergreen.webconsole.vaadin.tabs.Tab;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.annotation.PostConstruct;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.system.internal.SystemScope.tab")
@Navigable("/services")
@Tab("OSGi Services")
public class ServiceViewer extends VerticalLayout {

    public static final String SERVICE_ID_COLUMN = "serviceId";
    public static final String INTERFACES_COLUMN = "interfaces";
    public static final String BUNDLE_INFO_COLUMN = "bundleInfo";
    public static final String OPENED_COLUMN = "opened";

    @Inject
    private BundleContext bundleContext;

    private BeanItemContainer<ServiceReferenceItem> data = new BeanItemContainer<>(ServiceReferenceItem.class);
    private Table table;

    @PostConstruct
    public void createView() {
        setMargin(true);
        setSpacing(true);

        initHeader();
        initTable();
        initServiceReferences();
    }

    private void initHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        header.setSpacing(true);
        header.setMargin(true);

        Label title = new Label("OSGi Services");
        title.addStyleName("h1");
        title.setSizeUndefined();
        header.addComponent(title);
        header.setComponentAlignment(title, Alignment.MIDDLE_LEFT);

        final TextField filter = new TextField();
        filter.addTextChangeListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(final FieldEvents.TextChangeEvent event) {
                data.removeAllContainerFilters();
                Container.Filter or = new Or(
                        new SimpleStringFilter(SERVICE_ID_COLUMN, event.getText().trim(), true, false),
                        new SimpleStringFilter(INTERFACES_COLUMN, event.getText().trim(), true, false),
                        new SimpleStringFilter(BUNDLE_INFO_COLUMN, event.getText().trim(), true, false)
                );

                data.addContainerFilter(or);
            }
        });

        filter.setInputPrompt("Filter");
        filter.addShortcutListener(new ShortcutListener("Clear",
                ShortcutAction.KeyCode.ESCAPE, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                filter.setValue("");
                data.removeAllContainerFilters();
            }
        });
        header.addComponent(filter);
        header.setExpandRatio(filter, 1);
        header.setComponentAlignment(filter, Alignment.MIDDLE_LEFT);

        // Store the header in the vertical layout (this)
        addComponent(header);
    }

    private void initTable() {

        table = new Table();

        table.setColumnHeader(SERVICE_ID_COLUMN, "Service ID");
        table.setColumnHeader(INTERFACES_COLUMN, "Interfaces");
        table.setColumnHeader(BUNDLE_INFO_COLUMN, "Bundle");

        table.setSizeFull();
        table.setSortContainerPropertyId(SERVICE_ID_COLUMN);
        table.setSortAscending(true);
        //table.setSelectable(true);
        table.setColumnCollapsingAllowed(true);
        table.setColumnReorderingAllowed(true);
        table.setColumnWidth(SERVICE_ID_COLUMN, 70);
        table.setColumnWidth(BUNDLE_INFO_COLUMN, 300);

        table.setColumnAlignment(SERVICE_ID_COLUMN, Table.Align.CENTER);

        table.setContainerDataSource(data);
        table.setImmediate(true);
        table.setVisibleColumns(new Object[]{SERVICE_ID_COLUMN, INTERFACES_COLUMN, BUNDLE_INFO_COLUMN});

        table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(final ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    Property<Boolean> property = event.getItem().getItemProperty(OPENED_COLUMN);
                    if (property != null) {
                        // Gives details about service properties
                        Boolean old = property.getValue();
                        property.setValue(!old);
                        table.refreshRowCache();
                    }
                }
            }
        });

        addComponent(table);
        // Magic number: use all the empty space
        setExpandRatio(table, 1.5f);

    }

    private void initServiceReferences() {
        Bundle[] bundles = bundleContext.getBundles();
        table.removeAllItems();

        for (Bundle bundle : bundles) {
            if (bundle.getRegisteredServices() != null) {
                for (ServiceReference<?> reference : bundle.getRegisteredServices()) {
                    data.addBean(new ServiceReferenceItem(reference));
                }
            }
        }
        table.sort();
    }


}
