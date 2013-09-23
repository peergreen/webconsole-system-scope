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

import java.io.IOException;
import java.io.OutputStream;

import com.vaadin.server.Scrollable;
import com.vaadin.ui.Label;

/**
 * User: guillaume
 * Date: 17/09/13
 * Time: 09:28
 */
public class LabelOutputStream extends OutputStream {

    private enum Element {
        INSIDE, OUTSIDE
    }

    private final Label label;
    private final Scrollable panel;
    private StringBuilder builder = new StringBuilder();
    private Element mode = Element.OUTSIDE;

    public LabelOutputStream(final Label label, final Scrollable panel) {
        this.label = label;
        this.panel = panel;
    }

    @Override
    public void write(final int b) throws IOException {
        char c = (char) b;
        switch (c) {
            case '\n':
                builder.append("<br/>");
                appendHtml();
                break;
            case ' ':
                if (mode == Element.OUTSIDE) {
                    builder.append("&nbsp;");
                } else {
                    builder.append(c);
                }
                break;
            case '<':
                mode = Element.INSIDE;
                builder.append(c);
                break;
            case '>':
                mode = Element.OUTSIDE;
                builder.append(c);
                break;
            default:
                builder.append(c);

        }
    }

    private void appendHtml() {
        label.setValue(label.getValue() + builder.toString());
        panel.setScrollTop(panel.getScrollTop() + 20);
        builder = new StringBuilder();
    }
}
