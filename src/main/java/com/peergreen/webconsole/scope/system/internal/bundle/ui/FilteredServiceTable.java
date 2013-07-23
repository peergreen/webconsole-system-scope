package com.peergreen.webconsole.scope.system.internal.bundle.ui;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * User: guillaume
 * Date: 23/07/13
 * Time: 09:31
 */
public class FilteredServiceTable extends VerticalLayout {
    public static final int DEFAULT_PAGE_LENGTH = 10;
    public static final String SERVICE_ID = "service.id";
    public static final String SERVICE_DETAILS = "service.details";

    private HorizontalLayout header = new HorizontalLayout();
    private Label title;
    private String label;
    private TextField filter;
    private Table table;
    private IndexedContainer data = new IndexedContainer();

    public FilteredServiceTable(final String label) {
        this.label = label;

        title = new Label(label);
        title.addStyleName("h3");

        filter = new TextField();
        filter.setInputPrompt("filter...");

        header.addComponent(title);
        header.addComponent(filter);
        header.setComponentAlignment(filter, Alignment.MIDDLE_LEFT);
        header.setComponentAlignment(title, Alignment.MIDDLE_LEFT);
        header.setSpacing(true);

        data.addContainerProperty(SERVICE_ID, String.class, "");
        data.addContainerProperty(SERVICE_DETAILS, Label.class, null);

        table = new Table();
        table.setPageLength(DEFAULT_PAGE_LENGTH);
        table.setWidth("100%");
        table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
        table.setContainerDataSource(data);

        addComponent(header);
        addComponent(table);

        filter.addTextChangeListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(final FieldEvents.TextChangeEvent event) {
                data.removeAllContainerFilters();
                data.addContainerFilter(new SimpleStringFilter(SERVICE_DETAILS, event.getText().trim(), true, false));
            }
        });
        filter.addShortcutListener(new ShortcutListener("Clear",
                                                        ShortcutAction.KeyCode.ESCAPE,
                                                        null) {
            @Override
            public void handleAction(Object sender, Object target) {
                filter.setValue("");
                data.removeAllContainerFilters();
            }
        });


    }

    public void addService(final ServiceReference<?> reference) {
        Item item = data.getItem(data.addItem());
        String id = format("Service #%d", (Long) reference.getProperty(Constants.SERVICE_ID));
        item.getItemProperty(SERVICE_ID).setValue(id);

        String details = format("Types: %s", searchInterfaces(reference));
        String pid = (String) reference.getProperty(Constants.SERVICE_PID);
        if (pid != null) {
            details += "\n";
            details += "Service PID: " + pid;
        }
        item.getItemProperty(SERVICE_DETAILS).setValue(new Label(details, ContentMode.PREFORMATTED));
        title.setValue(format("%s (%d)", label, data.size()));
    }

    private String searchInterfaces(ServiceReference<?> reference) {
        List<String> interfaces = new ArrayList<>();
        String[] classes = (String[]) reference.getProperty(Constants.OBJECTCLASS);
        interfaces.addAll(Arrays.asList(classes));
        return interfaces.toString();
    }

    public boolean isEmpty() {
        return data.size() == 0;
    }
}
