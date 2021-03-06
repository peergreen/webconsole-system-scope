/**
 * Peergreen S.A.S. All rights reserved.
 * Proprietary and confidential.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.webconsole.scope.system.internal.bundle.ui;

import static java.lang.String.format;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.AbstractProperty;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
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
public class FilteredPackageTable extends VerticalLayout {
    public static final int DEFAULT_PAGE_LENGTH = 10;
    public static final String DESCRIPTION = "description";

    private HorizontalLayout header = new HorizontalLayout();
    private Label title;
    private String label;
    private TextField filter;
    private Table table;
    private IndexedContainer data = new IndexedContainer();

    public FilteredPackageTable(final String label) {
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

        data.addContainerProperty(DESCRIPTION, String.class, "aaa");

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
                data.addContainerFilter(new SimpleStringFilter(DESCRIPTION, event.getText().trim(), true, false));
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

    public void addPackage(final String description) {
        Item item = data.getItem(data.addItem());
        item.getItemProperty(DESCRIPTION).setValue(description);
        title.setValue(format("%s (%d)", label, data.size()));
    }
}
