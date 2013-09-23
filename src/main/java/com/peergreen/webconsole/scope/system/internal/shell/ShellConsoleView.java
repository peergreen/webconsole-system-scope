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
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
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

    @PostConstruct
    public void createView() {
        setMargin(true);
        setSpacing(true);

        context.getUI().getPage().getStyles().add(".console-font {font-family: Monaco, Menlo, Consolas, monospace !important; font-size:small !important;}");

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

        Label console = new Label();
        console.setContentMode(ContentMode.HTML);
        console.setSizeUndefined();
        console.addStyleName("console-font");

        Panel panel = new Panel(console);
        panel.setSizeFull();

        addComponent(panel);
        // Magic number: use all the empty space
        setExpandRatio(panel, 1.5f);

        OutputStream out = new HtmlAnsiOutputStream(new LabelOutputStream(console, panel));
        final PrintStream ps = new PrintStream(out);

        session = processor.createSession(new ByteArrayInputStream("".getBytes()), ps, ps);

        final TextField input = new TextField();
        input.setWidth("100%");
        input.setInputPrompt(">$ ");
        input.addShortcutListener(new ShortcutListener("Execute",
                                                       ShortcutAction.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                try {
                    String value = input.getValue();
                    ps.printf(">$ %s\n", value);
                    session.execute(value);
                } catch (Exception e) {
                    printException(ps, e);
                }
                input.setValue("");
            }
        });
        addComponent(input);

        doSessionBranding(ps);
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
    }

}
