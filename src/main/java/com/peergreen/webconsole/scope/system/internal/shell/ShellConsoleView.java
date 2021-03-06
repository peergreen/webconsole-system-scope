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

package com.peergreen.webconsole.scope.system.internal.shell;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Converter;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.HtmlAnsiOutputStream;
import org.ow2.shelbie.core.branding.BrandingService;

import com.peergreen.webconsole.Extension;
import com.peergreen.webconsole.ExtensionPoint;
import com.peergreen.webconsole.Inject;
import com.peergreen.webconsole.UIContext;
import com.peergreen.webconsole.navigator.Navigable;
import com.peergreen.webconsole.notifier.INotifierService;
import com.peergreen.webconsole.vaadin.tabs.Tab;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Guillaume Sauthier
 */
@Extension
@ExtensionPoint("com.peergreen.webconsole.scope.system.internal.SystemScope.tab")
@Navigable("/shell")
@Tab("OSGi Shell")
public class ShellConsoleView extends VerticalLayout {

    @Inject
    private CommandProcessor processor;

    @Inject
    private BrandingService brandingService;

    @Inject
    private UIContext context;

    @Inject
    private INotifierService notifier;

    private CommandSession session;

    private PrintStream printStream;

    @PostConstruct
    public void createView() {
        setMargin(true);
        setSpacing(true);

        context.getUI().getPage().getStyles().add(".console-font {font-family: Menlo, Consolas, monospace !important; font-size:small !important;}");

        initHeader();
        initConsole();
    }

    private void doSessionBranding(final PrintStream ps) {
        // 1. Print the banner
        session.getConsole().println(brandingService.getBanner(true));

        // 2. Inject variables
        for (Map.Entry<String, Object> entry : brandingService.getVariables().entrySet()) {
            session.put(entry.getKey(), entry.getValue());
        }

        // 3. Execute customization script
        if (brandingService.getScript() != null) {
            for (String line : brandingService.getScript()) {
                try {
                    Object result = session.execute(line);
                    //session.put(Constants.LAST_RESULT_VARIABLE, result);
                    if (result != null) {
                        session.getConsole().println(session.format(result, Converter.INSPECT));
                    }
                } catch (Exception e) {
                    printException(ps, e);
                }
            }
        }
    }
    private void initHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidth("100%");
        header.setSpacing(true);
        header.setMargin(true);

        Label title = new Label("Shell Console");
        title.addStyleName("h1");
        title.setSizeUndefined();
        header.addComponent(title);
        header.setComponentAlignment(title, Alignment.MIDDLE_LEFT);

        // Store the header in the vertical layout (this)
        addComponent(header);
    }

    private void initConsole() {

        Table table = new Table();
        table.addStyleName("console-font");
        table.addStyleName("borderless");
        table.setColumnHeaderMode(Table.ColumnHeaderMode.HIDDEN);
        IndexedContainer container = new IndexedContainer();
        table.setContainerDataSource(container);
        table.setImmediate(true);
        table.setSizeFull();
        table.addContainerProperty("line", Label.class, null);

        addComponent(table);
        // Magic number: use all the empty space
        setExpandRatio(table, 1.5f);

        OutputStream out = new HtmlAnsiOutputStream(new WebConsoleOutputStream(table, container));
        this.printStream = new PrintStream(out);

        session = processor.createSession(new ByteArrayInputStream("".getBytes()), printStream, printStream);

        final TextField input = new TextField();
        input.setWidth("100%");
        input.setInputPrompt(">$ ");

        final ShortcutListener shortcut = new ShortcutListener("Execute",
                                                               ShortcutAction.KeyCode.ENTER,
                                                               null) {
            @Override
            public void handleAction(Object sender, Object target) {
                try {
                    String value = input.getValue();
                    printStream.printf(">$ %s\n", value);
                    Object result = session.execute(value);
                    //session.put(Constants.LAST_RESULT_VARIABLE, result);
                    if (result != null) {
                        session.getConsole().println(session.format(result, Converter.INSPECT));
                    }
                } catch (Exception e) {
                    printException(printStream, e);
                }
                input.setValue("");
            }
        };

        // Install the shortcut listener only when the input text-field has the focus
        input.addFocusListener(new FieldEvents.FocusListener() {
            @Override
            public void focus(final FieldEvents.FocusEvent event) {
                input.addShortcutListener(shortcut);
            }
        });
        input.addBlurListener(new FieldEvents.BlurListener() {
            @Override
            public void blur(final FieldEvents.BlurEvent event) {
                input.removeShortcutListener(shortcut);
            }
        });

        addComponent(input);

        doSessionBranding(printStream);
    }

    private void printException(final PrintStream ps, final Exception e) {
        // Print an error line in the console
        Ansi buffer = Ansi.ansi().render("@|red %s|@: %s", e.getClass().getSimpleName(), e.getMessage());
        ps.println(buffer);
        notifier.addNotification(e.getMessage());
    }

    @PreDestroy
    public void close() {
        session.close();
        printStream.close();
    }

}
