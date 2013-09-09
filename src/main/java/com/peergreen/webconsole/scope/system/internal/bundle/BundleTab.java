package com.peergreen.webconsole.scope.system.internal.bundle;

import static com.peergreen.webconsole.scope.system.internal.bundle.BundleHelper.getHeader;
import static java.lang.String.format;
import static org.osgi.framework.Constants.BUNDLE_ACTIVATIONPOLICY;
import static org.osgi.framework.Constants.BUNDLE_ACTIVATOR;
import static org.osgi.framework.Constants.BUNDLE_CATEGORY;
import static org.osgi.framework.Constants.BUNDLE_CLASSPATH;
import static org.osgi.framework.Constants.BUNDLE_CONTACTADDRESS;
import static org.osgi.framework.Constants.BUNDLE_COPYRIGHT;
import static org.osgi.framework.Constants.BUNDLE_DESCRIPTION;
import static org.osgi.framework.Constants.BUNDLE_DOCURL;
import static org.osgi.framework.Constants.BUNDLE_LOCALIZATION;
import static org.osgi.framework.Constants.BUNDLE_NAME;
import static org.osgi.framework.Constants.BUNDLE_NATIVECODE;
import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.BUNDLE_UPDATELOCATION;
import static org.osgi.framework.Constants.BUNDLE_VENDOR;
import static org.osgi.framework.Constants.BUNDLE_VERSION;
import static org.osgi.framework.Constants.FRAGMENT_HOST;
import static org.osgi.framework.Constants.PROVIDE_CAPABILITY;
import static org.osgi.framework.Constants.REQUIRE_BUNDLE;
import static org.osgi.framework.Constants.REQUIRE_CAPABILITY;

import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.LinkedHashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleWiring;

import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.scope.system.internal.bundle.actions.StartBundleClickListener;
import com.peergreen.webconsole.scope.system.internal.bundle.actions.StopBundleClickListener;
import com.peergreen.webconsole.scope.system.internal.bundle.actions.UninstallBundleClickListener;
import com.peergreen.webconsole.scope.system.internal.bundle.actions.UpdateBundleClickListener;
import com.peergreen.webconsole.scope.system.internal.bundle.ui.FilteredPackageTable;
import com.peergreen.webconsole.scope.system.internal.bundle.ui.FilteredServiceTable;
import com.peergreen.webconsole.scope.system.internal.bundle.ui.Section;
import com.vaadin.server.ClassResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

/**
 * User: guillaume
 * Date: 22/07/13
 * Time: 12:02
 */
public class BundleTab extends VerticalLayout {
    private final Bundle bundle;
    private final INotifierService notifierService;
    private static final Map<String, String> HEADERS = new LinkedHashMap<>();

    static {
        HEADERS.put(BUNDLE_SYMBOLICNAME, "Symbolic Name");
        HEADERS.put(BUNDLE_VERSION, "Version");
        HEADERS.put(FRAGMENT_HOST, "Fragment Host");
        HEADERS.put(BUNDLE_NAME, "Name");
        HEADERS.put(BUNDLE_UPDATELOCATION, "Update Location");
        HEADERS.put(BUNDLE_ACTIVATOR, "Activator");
        HEADERS.put(BUNDLE_CLASSPATH, "Class-Path");
        HEADERS.put(REQUIRE_BUNDLE, "Required Bundle(s)");
        HEADERS.put(PROVIDE_CAPABILITY, "Provided Capabilities");
        HEADERS.put(REQUIRE_CAPABILITY, "Required Capabilities");
        HEADERS.put(BUNDLE_NATIVECODE, "Native Code");
        HEADERS.put(BUNDLE_ACTIVATIONPOLICY, "Activation Policy");
        HEADERS.put(BUNDLE_CATEGORY, "Category");
        HEADERS.put(BUNDLE_COPYRIGHT, "Copyright");
        HEADERS.put(BUNDLE_DESCRIPTION, "Description");
        HEADERS.put(BUNDLE_DOCURL, "Documentation URL");
        HEADERS.put(BUNDLE_CONTACTADDRESS, "Contact Address");
        HEADERS.put(BUNDLE_VENDOR, "Vendor");
        HEADERS.put(BUNDLE_LOCALIZATION, "Localization");
    }

    public BundleTab(final Bundle bundle, final INotifierService notifierService) {
        this.bundle = bundle;
        this.notifierService = notifierService;
        init();
    }

    private void init() {

        setMargin(true);
        setSpacing(true);

        // ----------------------------------------------------
        // Title
        // ----------------------------------------------------

        HorizontalLayout header = new HorizontalLayout();
        header.setSpacing(true);
        header.setMargin(true);

        Label title = new Label(format("Bundle %d: %s (%s)",
                bundle.getBundleId(),
                getHeader(bundle, Constants.BUNDLE_NAME),
                bundle.getVersion()));
        title.addStyleName("h1");
        header.addComponent(title);
        header.setComponentAlignment(title, Alignment.MIDDLE_LEFT);

        addComponent(header);

        // ----------------------------------------------------
        // Action(s) Bar
        // ----------------------------------------------------

        HorizontalLayout actions = new HorizontalLayout();
        if (BundleHelper.isState(bundle, Bundle.INSTALLED) || BundleHelper.isState(bundle, Bundle.RESOLVED)) {
            Button changeState = new Button();
            changeState.addClickListener(new StartBundleClickListener(bundle, notifierService));
            //changeState.addStyleName("no-padding");
            changeState.setCaption("Start");
            changeState.setIcon(new ClassResource(getClass(), "/images/32x32/go-next.png"));
            actions.addComponent(changeState);
        }
        if (BundleHelper.isState(bundle, Bundle.ACTIVE)) {
            Button changeState = new Button();
            changeState.addClickListener(new StopBundleClickListener(bundle, notifierService));
            //changeState.addStyleName("no-padding");
            changeState.setCaption("Stop");
            changeState.setIcon(new ClassResource(getClass(), "/images/32x32/media-record.png"));
            actions.addComponent(changeState);
        }

        // Update
        Button update = new Button();
        update.addClickListener(new UpdateBundleClickListener(bundle, notifierService));
        //update.addStyleName("no-padding");
        update.setCaption("Update");
        update.setIcon(new ClassResource(getClass(), "/images/32x32/view-refresh.png"));
        actions.addComponent(update);

        // Trash
        Button trash = new Button();
        trash.addClickListener(new UninstallBundleClickListener(bundle, notifierService));
        //trash.addStyleName("no-padding");
        trash.setCaption("Remove");
        trash.setIcon(new ClassResource(getClass(), "/images/32x32/user-trash-full.png"));
        actions.addComponent(trash);
        addComponent(actions);
        setComponentAlignment(actions, Alignment.MIDDLE_RIGHT);

        // ----------------------------------------------------
        // Standard Section
        // ----------------------------------------------------

        Table table = new Table();
        table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
        table.setWidth("100%");

        Section mainSection = new Section("Standard", table);
        addComponent(mainSection);

        table.addContainerProperty("label", Label.class, null);
        table.addContainerProperty("value", Label.class, null);

        table.addItem(new Object[]{label("Bundle ID"), label(String.valueOf(bundle.getBundleId()))}, "bundle.id");
        for (Map.Entry<String, String> entry : HEADERS.entrySet()) {
            String value = getHeader(bundle, entry.getKey());
            if (value != null) {
                table.addItem(new Object[]{label(entry.getValue()), label(value)}, entry.getKey());
            }
        }
        table.addItem(new Object[]{label("Location"), label(bundle.getLocation())}, "bundle.location");
        Date date = new Date(bundle.getLastModified());
        table.addItem(new Object[]{label("Last Modified"), label(date.toString())}, "last.modified");

        // ----------------------------------------------------
        // Packages Section
        // ----------------------------------------------------

        FilteredPackageTable exported = new FilteredPackageTable("Exported");

        FilteredPackageTable imported = new FilteredPackageTable("Imported");

        GridLayout packages = new GridLayout(2, 1);
        packages.addComponent(exported);
        packages.addComponent(imported);
        packages.setSpacing(true);
        packages.setWidth("100%");

        Section packagesSection = new Section("Packages", packages);
        addComponent(packagesSection);

        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        if (wiring != null) {
            for (BundleCapability capability : wiring.getCapabilities(PackageNamespace.PACKAGE_NAMESPACE)) {
                String name = (String) capability.getAttributes().get(PackageNamespace.PACKAGE_NAMESPACE);
                Version version = (Version) capability.getAttributes().get(PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE);
                exported.addPackage(format("%s (%s)", name, version));
            }
            for (BundleRequirement requirement : wiring.getRequirements(PackageNamespace.PACKAGE_NAMESPACE)) {
                String filter = requirement.getDirectives().get(PackageNamespace.REQUIREMENT_FILTER_DIRECTIVE);
                imported.addPackage(filter);
            }
        }

        // ----------------------------------------------------
        // Services Section
        // ----------------------------------------------------

        FilteredServiceTable registered = new FilteredServiceTable("Registered");

        FilteredServiceTable used = new FilteredServiceTable("Used Services");

        VerticalLayout services = new VerticalLayout(registered, used);
        services.setSpacing(true);
        services.setWidth("100%");

        ServiceReference<?>[] registeredServices = bundle.getRegisteredServices();
        if (registeredServices != null) {
            for (ServiceReference<?> reference : registeredServices) {
                registered.addService(reference);
            }
        }

        ServiceReference<?>[] inUseServices = bundle.getServicesInUse();
        if (inUseServices != null) {
            for (ServiceReference<?> reference : inUseServices) {
                used.addService(reference);
            }
        }

        if (!registered.isEmpty() || !used.isEmpty()) {
            Section servicesSection = new Section("Services", services);
            addComponent(servicesSection);
        }


        // ----------------------------------------------------
        // Raw Manifest Section
        // ----------------------------------------------------

        Page.Styles styles = Page.getCurrent().getStyles();
        styles.add(".monospaced-font {font-family: monospace !important; }");

        Table manifest = new Table();
        manifest.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
        manifest.setWidth("100%");
        manifest.addStyleName("monospaced-font");
        manifest.setPageLength(15);
        manifest.addContainerProperty("name", String.class, null);
        manifest.addContainerProperty("value", String.class, null);

        Dictionary<String, String> headers = bundle.getHeaders();
        for (String key : Collections.list(headers.keys())) {
            manifest.addItem(new Object[]{key, headers.get(key)}, null);
        }

        Section manifestSection = new Section("Manifest", manifest);
        addComponent(manifestSection);

    }

    private Label label(String value) {
        return new Label(value);
    }


}
