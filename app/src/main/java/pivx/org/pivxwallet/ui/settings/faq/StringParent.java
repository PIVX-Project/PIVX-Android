package pivx.org.pivxwallet.ui.settings.faq;

import com.bignerdranch.expandablerecyclerview.model.Parent;
import com.google.common.collect.Lists;

import java.util.List;

class StringParent implements Parent<String> {

    public String text;
    public String body;

    public StringParent(String text, String body) {
        this.text = text;
        this.body = body;
    }

    @Override
    public List getChildList() {
        return Lists.newArrayList(body);
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
