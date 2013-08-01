package com.peergreen.webconsole.scope.system.internal;

import com.peergreen.webconsole.Constants;
import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.INotifierService;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.Link;
import com.peergreen.webconsole.Ready;
import com.peergreen.webconsole.Scope;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.Unlink;
import com.peergreen.webconsole.navigator.Navigable;
import com.peergreen.webconsole.navigator.NavigableContext;
import com.peergreen.webconsole.navigator.Navigate;
import com.peergreen.webconsole.vaadin.CloseTabListener;
import com.peergreen.webconsole.vaadin.SelectedTabListener;
import com.peergreen.webconsole.utils.UrlFragment;
import com.peergreen.webconsole.vaadin.DefaultTab;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;

import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mohammed Boukada
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope")
@Navigable
@Scope("system")
public class SystemScope extends TabSheet {

    private DefaultTab defaultTab = new DefaultTab(this);
    private Map<String, Component> components = new ConcurrentHashMap<>();

    @Inject
    private UIContext uiContext;

    @Inject
    private INotifierService notifierService;

    @Ready
    public void init() {
        defaultTab.setUi(uiContext.getUI());
        addTab(defaultTab, "System", null, 0);
        setSizeFull();
        setCloseHandler(new CloseTabListener(notifierService));
        addSelectedTabChangeListener(new SelectedTabListener(uiContext.getViewNavigator(), defaultTab));
    }

    @Link("tab")
    public void addTabs(Component tab, Dictionary properties) {
        tab.setSizeFull();
        addTab(tab, (String) properties.get("tab.value")).setClosable(true);
        defaultTab.addExtension(tab, (String) properties.get("tab.value"));
        String alias = (String) properties.get(Constants.EXTENSION_ALIAS);
        if (alias != null) {
            components.put(alias, tab);
        }
    }

    @Unlink("tab")
    public void removeTabs(Component tab, Dictionary properties) {
        removeComponent(tab);
        defaultTab.removeExtension((String) properties.get("tab.value"));
        String alias = (String) properties.get(Constants.EXTENSION_ALIAS);
        if (alias != null && components.containsKey(alias)){
            components.remove(alias);
        }
    }

    @Navigate
    public Component navigate(NavigableContext context) {
        Component tab = components.get(UrlFragment.getFirstFragment(context.getPath()));
        context.setPath(UrlFragment.subFirstFragment(context.getPath()));
        if (tab != null) {
            setSelectedTab(tab);
        }
        return tab;
    }
}
