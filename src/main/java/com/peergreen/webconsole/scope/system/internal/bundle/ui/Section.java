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

import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * User: guillaume
 * Date: 22/07/13
 * Time: 19:12
 */
public class Section extends VerticalLayout {
    private final CssLayout layout = new CssLayout();
    private final Label label;
    private final Component component;

    public Section(final String title, final Component component) {
        this(new Label(title), component);
    }

    public Section(final Label label, final Component component) {
        this.label = label;
        this.component = component;
        init();
    }

    private void init() {
        setMargin(true);
        setSpacing(true);
        label.addStyleName("h2");
        layout.addComponent(label);

        addComponent(layout);
        addComponent(component);

        layout.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(final LayoutEvents.LayoutClickEvent event) {
                if (event.getClickedComponent().equals(label)) {
                    component.setVisible(!component.isVisible());
                }
            }
        });
    }

    public Label getLabel() {
        return label;
    }

    public Component getComponent() {
        return component;
    }
}
