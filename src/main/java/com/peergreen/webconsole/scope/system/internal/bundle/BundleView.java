package com.peergreen.webconsole.scope.system.internal.bundle;

import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.ISecurityManager;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Ready;
import com.peergreen.webconsole.navigator.NavigableContext;
import com.peergreen.webconsole.navigator.Navigate;
import com.peergreen.webconsole.scope.system.SystemTab;
import com.peergreen.webconsole.scope.system.internal.bundle.actions.StartBundleClickListener;
import com.peergreen.webconsole.scope.system.internal.bundle.actions.StopBundleClickListener;
import com.peergreen.webconsole.scope.system.internal.bundle.actions.UninstallBundleClickListener;
import com.peergreen.webconsole.scope.system.internal.bundle.actions.UpdateBundleClickListener;
import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.ClassResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import org.apache.felix.ipojo.annotations.Invalidate;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint(value = "com.peergreen.webconsole.scope.system.internal.SystemScope.tab", alias = "/bundles")
@SystemTab("OSGi Bundles")
public class BundleView extends VerticalLayout {

    private static final String BUNDLE_ID_COLUMN = "bundleId";
    private static final String NAME_COLUMN = "bundleName";
    private static final String VERSION_COLUMN = "version";
    private static final String STATE_COLUMN = "state";
    @Inject
    private BundleContext bundleContext;
    @Inject
    private ISecurityManager securityManager;
    @Inject
    private INotifierService notifierService;

    private Table table;
    private BeanItemContainer<BundleItem> data = new BeanItemContainer<>(BundleItem.class);
    private BundleTracker<BeanItem<BundleItem>> tracker;

    private TabSheet tabSheet = new TabSheet();
    private Map<Long, Component> openTabs = new ConcurrentHashMap<>();

    @Ready
    public void createView() {
        setMargin(true);
        setSpacing(true);

        /*
        Page.Styles styles = Page.getCurrent().getStyles();
        styles.add(".no-padding {padding: 0em 0em 0em 0em !important; }");
        */

        HorizontalLayout header = new HorizontalLayout();
//        header.setWidth("100%");
        header.setSpacing(true);
        header.setMargin(true);

        Label title = new Label("OSGi Bundles");
        title.addStyleName("h1");
//        title.setSizeUndefined();
        header.addComponent(title);
        header.setComponentAlignment(title, Alignment.MIDDLE_LEFT);

        final TextField filter = new TextField();
        filter.addTextChangeListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(final FieldEvents.TextChangeEvent event) {
                data.removeAllContainerFilters();
                Container.Filter or = new Or(
                        new SimpleStringFilter(BUNDLE_ID_COLUMN, event.getText().trim(), true, false),
                        new SimpleStringFilter(NAME_COLUMN, event.getText().trim(), true, false),
                        new SimpleStringFilter(VERSION_COLUMN, event.getText().trim(), true, false),
                        new SimpleStringFilter(STATE_COLUMN, event.getText().trim(), true, false)
                );

                data.addContainerFilter(or);
            }
        });

        filter.setInputPrompt("Filter");
        filter.addShortcutListener(new ShortcutListener("Clear",
                                                        ShortcutAction.KeyCode.ESCAPE,
                                                        null) {
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

        addComponent(tabSheet);

        table = new Table() {
            @Override
            protected String formatPropertyValue(final Object rowId, final Object colId, final Property<?> property) {
                if ("state".equals(colId)) {
                    return getStateString((Integer) property.getValue());
                }
                return super.formatPropertyValue(rowId, colId, property);
            }
        };
        table.setContainerDataSource(data);
        table.setSizeFull();
        table.setSortContainerPropertyId(BUNDLE_ID_COLUMN);
        table.setSortAscending(true);
        table.setImmediate(true);
        table.setColumnHeader("bundleId", "Bundle ID");
        table.setColumnHeader("bundleName", "Bundle Name");
        table.setColumnHeader("version", "Version");
        table.setColumnHeader("state", "State");
        table.setColumnHeader("actions", "Actions");

        table.setColumnWidth("bundleId", 100);

        table.setColumnAlignment("bundleId", Table.Align.CENTER);
        table.setColumnAlignment("state", Table.Align.CENTER);
        table.setColumnAlignment("version", Table.Align.CENTER);

        table.addGeneratedColumn("actions", new Table.ColumnGenerator() {
            @Override
            public Object generateCell(final Table source, final Object itemId, final Object columnId) {
                HorizontalLayout layout = new HorizontalLayout();
                BeanItem<BundleItem> item = (BeanItem<BundleItem>) source.getContainerDataSource().getItem(itemId);
                Bundle bundle = item.getBean().getBundle();
                int state = bundle.getState();
                if (BundleHelper.isState(bundle, Bundle.INSTALLED) || BundleHelper.isState(bundle, Bundle.RESOLVED)) {
                    Button changeState = new Button();
                    changeState.addClickListener(new StartBundleClickListener(item.getBean().getBundle(), notifierService));
                    //changeState.addStyleName("no-padding");
                    changeState.setCaption("Start");
                    //changeState.setIcon(new ClassResource(BundleViewer.class, "/images/go-next.png"));
                    if (!securityManager.isUserInRole("admin")) {
                        changeState.setDisableOnClick(true);
                    }
                    layout.addComponent(changeState);
                }
                if (BundleHelper.isState(bundle, Bundle.ACTIVE)) {
                    Button changeState = new Button();
                    changeState.addClickListener(new StopBundleClickListener(item.getBean().getBundle(), notifierService));
                    //changeState.addStyleName("no-padding");
                    changeState.setCaption("Stop");
                    if (!securityManager.isUserInRole("admin")) {
                        changeState.setDisableOnClick(true);
                    }
                    //changeState.setIcon(new ClassResource(BundleViewer.class, "/images/media-record.png"));
                    layout.addComponent(changeState);
                }

                // Update
                Button update = new Button();
                update.addClickListener(new UpdateBundleClickListener(item.getBean().getBundle(), notifierService));
                //update.addStyleName("no-padding");
                update.setCaption("Update");
                if (!securityManager.isUserInRole("admin")) {
                    update.setDisableOnClick(true);
                }
                //update.setIcon(new ClassResource(BundleViewer.class, "/images/view-refresh.png"));
                layout.addComponent(update);

                // Trash
                Button trash = new Button();
                trash.addClickListener(new UninstallBundleClickListener(item.getBean().getBundle(), notifierService));
                //trash.addStyleName("no-padding");
                trash.setCaption("Delete");
                if (!securityManager.isUserInRole("admin")) {
                    trash.setDisableOnClick(true);
                }
                //trash.setIcon(new ClassResource(BundleViewer.class, "/images/user-trash-full.png"));
                layout.addComponent(trash);

                return layout;
            }
        });

        table.setVisibleColumns("bundleId", "bundleName", "version", "state", "actions");

        table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(final ItemClickEvent event) {
                if (event.isDoubleClick()) {
                    BeanItem<BundleItem> item = (BeanItem<BundleItem>) table.getContainerDataSource().getItem(event.getItemId());
                    Bundle bundle = item.getBean().getBundle();

                    TabSheet.Tab tab = tabSheet.addTab(new BundleTab(bundle, notifierService),
                                                       "Bundle " + bundle.getBundleId(),
                                                       new ClassResource(BundleView.class, "/images/22x22/package-x-generic.png"));
                    tab.setClosable(true);
                    tabSheet.setSelectedTab(tab);
                }
            }
        });

        createBundleTracker();

        tabSheet.setSizeFull();
        tabSheet.addTab(table, "Bundles", new ClassResource(BundleView.class, "/images/22x22/user-home.png"));
        setExpandRatio(tabSheet, 1.5f);

        tabSheet.setCloseHandler(new TabSheet.CloseHandler() {
            @Override
            public void onTabClose(TabSheet tabsheet, Component tabContent) {
                for (Map.Entry<Long, Component> tab : openTabs.entrySet()) {
                    if (tabContent.equals(tab.getValue()))
                        openTabs.remove(tab.getKey());
                }
                tabsheet.removeComponent(tabContent);
            }
        });
    }

    @Invalidate
    public void close() {
        tracker.close();
    }

    private void createBundleTracker() {
        tracker = new BundleTracker<BeanItem<BundleItem>>(bundleContext, states(), new BundleTrackerCustomizer<BeanItem<BundleItem>>() {
            @Override
            public BeanItem<BundleItem> addingBundle(final Bundle bundle, final BundleEvent event) {
                return data.addBean(new BundleItem(bundle));
            }

            @Override
            public void modifiedBundle(final Bundle bundle, final BundleEvent event, final BeanItem<BundleItem> item) {
                // Refresh the table
                table.refreshRowCache();
            }

            @Override
            public void removedBundle(final Bundle bundle, final BundleEvent event, final BeanItem<BundleItem> item) {
                data.removeItem(item);
            }
        });
        tracker.open();
        table.sort();
    }

    private int states() {
        return Bundle.INSTALLED |
                Bundle.RESOLVED |
                Bundle.STOPPING |
                Bundle.STARTING |
                Bundle.ACTIVE |
                Bundle.UNINSTALLED;
    }

    private String getStateString(int state) {
        switch (state) {
            case Bundle.INSTALLED:
                return "INSTALLED";
            case Bundle.RESOLVED:
                return "RESOLVED";
            case Bundle.STARTING:
                return "STARTING";
            case Bundle.ACTIVE:
                return "ACTIVE";
            case Bundle.STOPPING:
                return "STOPPING";
            case Bundle.UNINSTALLED:
                return "UNINSTALLED";
            default:
                return "UNKNOWN";
        }
    }

    @Navigate
    public Component navigate(NavigableContext context) {
        if (showBundleDetails(context.getPath())) {
            Long bundleId = Long.parseLong(context.getPath().substring(1));
            Component tab;
            if (!openTabs.containsKey(bundleId)) {
                Bundle bundle = bundleContext.getBundle(bundleId);
                tab = new BundleTab(bundle, notifierService);
                tabSheet.addTab(tab,
                        "Bundle " + bundle.getBundleId(),
                        new ClassResource(BundleView.class, "/images/22x22/package-x-generic.png")).setClosable(true);
                openTabs.put(bundleId, tab);
            } else {
                tab = openTabs.get(bundleId);
            }
            tabSheet.setSelectedTab(tab);
        }
        return null;
    }

    private boolean showBundleDetails(String param) {
        return param.matches("/[0-9]*");
    }
}
