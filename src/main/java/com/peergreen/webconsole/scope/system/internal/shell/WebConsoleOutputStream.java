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

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;

/**
 * User: guillaume
 * Date: 17/09/13
 * Time: 09:28
 */
public class WebConsoleOutputStream extends OutputStream {

    public static final int DEFAULT_MAX_SIZE = 4096;

    private enum Element {
        INSIDE, OUTSIDE
    }

    private final Table table;
    private final IndexedContainer container;

    private StringBuilder builder = new StringBuilder();

    private Object lastItemId = null;
    private boolean scrollLock = false;
    private Element mode = Element.OUTSIDE;

    /**
     * Current number of lines in the buffer.
     */
    private int size = 0;

    /**
     * Configurable maximum buffer size (line count)
     */
    private int sizeMax = DEFAULT_MAX_SIZE;


    public WebConsoleOutputStream(final Table table, final IndexedContainer container) {
        this.table = table;
        this.container = container;
    }

    public int getSize() {
        return size;
    }

    public int getSizeMax() {
        return sizeMax;
    }

    public void setSizeMax(final int sizeMax) {
        this.sizeMax = sizeMax;
    }

    public boolean isScrollLock() {
        return scrollLock;
    }

    public void setScrollLock(final boolean scrollLock) {
        this.scrollLock = scrollLock;
    }

    @Override
    public void write(final int b) throws IOException {
        char c = (char) b;
        switch (c) {
            case '\n':
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

        Object itemId = container.addItemAfter(lastItemId);
        lastItemId = itemId;
        Item item = container.getItem(itemId);
        item.getItemProperty("line").setValue(new Label(builder.toString(), ContentMode.HTML));

        size++;
        // Reduce the buffer if required
        if (size > sizeMax) {
            container.removeItem(container.getIdByIndex(0));
        }

        table.refreshRowCache();

        // Scroll the page if required
        if (!scrollLock) {
            table.setCurrentPageFirstItemId(lastItemId);
        }

        builder = new StringBuilder();
    }
}
