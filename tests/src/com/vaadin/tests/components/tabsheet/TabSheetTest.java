package com.vaadin.tests.components.tabsheet;

import java.util.LinkedHashMap;

import com.vaadin.terminal.Resource;
import com.vaadin.tests.components.AbstractComponentContainerTest;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.CloseHandler;
import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.TabSheet.Tab;

public class TabSheetTest extends AbstractComponentContainerTest<TabSheet>
        implements SelectedTabChangeListener {

    private Command<TabSheet, Integer> setTabCaption = new Command<TabSheet, Integer>() {

        public void execute(TabSheet c, Integer value, Object data) {
            c.getTab(value).setCaption((String) data);

        }
    };
    private Command<TabSheet, Integer> setTabIcon = new Command<TabSheet, Integer>() {

        public void execute(TabSheet c, Integer value, Object data) {
            c.getTab(value).setIcon((Resource) data);

        }
    };
    private Command<TabSheet, Integer> setTabClosable = new Command<TabSheet, Integer>() {

        public void execute(TabSheet c, Integer value, Object data) {
            c.getTab(value).setClosable((Boolean) data);
        }
    };
    private Command<TabSheet, Boolean> setCloseHandlerListener = new Command<TabSheet, Boolean>() {

        public void execute(TabSheet c, Boolean value, Object data) {
            if (value) {
                c.setCloseHandler(new CloseHandler() {
                    public void onTabClose(TabSheet tabsheet, Component c) {
                        tabClosed(tabsheet, tabsheet.getTab(c));
                        tabsheet.removeComponent(c);
                    }

                });
            } else {
                c.setCloseHandler(new CloseHandler() {
                    public void onTabClose(TabSheet tabsheet, Component c) {
                        tabsheet.removeComponent(c);
                    }
                });
            }

        }
    };
    private Command<TabSheet, Boolean> setSelectedTabListener = new Command<TabSheet, Boolean>() {

        public void execute(TabSheet c, Boolean value, Object data) {
            if (value) {
                c.addListener((SelectedTabChangeListener) TabSheetTest.this);
            } else {
                c.removeListener((SelectedTabChangeListener) TabSheetTest.this);
            }

        }
    };

    private Command<TabSheet, Integer> selectTab = new Command<TabSheet, Integer>() {
        public void execute(TabSheet c, Integer index, Object data) {
            c.setSelectedTab(c.getTab(index).getComponent());
        }
    };
    private Command<TabSheet, Boolean> hideTabs = new Command<TabSheet, Boolean>() {

        public void execute(TabSheet c, Boolean value, Object data) {
            c.hideTabs(value);

        }
    };

    @Override
    protected Class<TabSheet> getTestClass() {
        return TabSheet.class;
    }

    @Override
    protected void createActions() {
        super.createActions();
        createSetTabCaptionIcon(CATEGORY_FEATURES);
        createSelectTab(CATEGORY_FEATURES);
        createClosableToggle(CATEGORY_FEATURES);
        createCloseHandlerToggle(CATEGORY_LISTENERS);
        createSelectListenerToggle(CATEGORY_LISTENERS);
        createHideTabsToggle(CATEGORY_FEATURES);

        // TODO
        // Insert tab at x

        for (TabSheet ts : getTestComponents()) {
            // Workaround for #6191
            ts.setWidth("100px");
        }
    }

    private void createHideTabsToggle(String category) {
        createBooleanAction("Hide tabs", category, false, hideTabs);

    }

    private void createSelectListenerToggle(String category) {
        createBooleanAction("Selected tab listener", category, false,
                setSelectedTabListener);

    }

    private void createCloseHandlerToggle(String category) {
        createBooleanAction("Close event listener (handler)", category, false,
                setCloseHandlerListener);

    }

    private void createClosableToggle(String category) {
        String closableCategory = "Set tab closable";
        createCategory(closableCategory, category);
        for (int i = 0; i < 20; i++) {
            String tabClosableCategory = "Tab " + i + " closable";

            createCategory(tabClosableCategory, closableCategory);
            createClickAction("true", tabClosableCategory, setTabClosable, i,
                    true);
            createClickAction("false", tabClosableCategory, setTabClosable, i,
                    false);

        }
    }

    private void createSelectTab(String category) {
        String selectTabCategory = "Select tab";
        createCategory(selectTabCategory, category);
        for (int i = 0; i < 20; i++) {
            createClickAction("Select tab " + i, selectTabCategory, selectTab,
                    i);

        }
    }

    private void createSetTabCaptionIcon(String category) {
        String captionCategory = "Set tab caption";
        String iconCategory = "Set tab icon";
        createCategory(captionCategory, category);
        createCategory(iconCategory, category);

        String captionOptions[] = new String[] { "", "{id}", "Tab {id}",
                "A long caption for tab {id}" };
        LinkedHashMap<String, Resource> iconOptions = new LinkedHashMap<String, Resource>();
        iconOptions.put("-", null);
        iconOptions.put("16x16 (cachable)", ICON_16_USER_PNG_CACHEABLE);
        iconOptions.put("16x16 (uncachable)", ICON_16_USER_PNG_UNCACHEABLE);
        iconOptions.put("32x32 (cachable)", ICON_32_ATTENTION_PNG_CACHEABLE);
        iconOptions
                .put("32x32 (uncachable)", ICON_32_ATTENTION_PNG_UNCACHEABLE);
        iconOptions.put("64x64 (cachable)", ICON_64_EMAIL_REPLY_PNG_CACHEABLE);
        iconOptions.put("64x64 (uncachable)",
                ICON_64_EMAIL_REPLY_PNG_UNCACHEABLE);

        for (int i = 0; i < 20; i++) {
            String tabCaptionCategory = "Tab " + i + " caption";
            String tabIconCategory = "Tab " + i + " icon";

            createCategory(tabCaptionCategory, captionCategory);
            createCategory(tabIconCategory, iconCategory);

            createClickAction("(null)", tabCaptionCategory, setTabCaption,
                    Integer.valueOf(i), null);
            createClickAction("(null)", tabIconCategory, setTabIcon,
                    Integer.valueOf(i), null);

            for (String option : captionOptions) {
                option = option.replace("{id}", String.valueOf(i));
                createClickAction(option, tabCaptionCategory, setTabCaption,
                        Integer.valueOf(i), option);
            }

            for (String option : iconOptions.keySet()) {
                Resource icon = iconOptions.get(option);
                createClickAction(option, tabIconCategory, setTabIcon,
                        Integer.valueOf(i), icon);
            }

        }

    }

    private void tabClosed(TabSheet tabSheet, Tab tab) {
        log("Tab " + tabSheet.getTabPosition(tab) + " closed");
    }

    public void selectedTabChange(SelectedTabChangeEvent event) {
        TabSheet ts = event.getTabSheet();
        log("Tab " + ts.getTabPosition(ts.getTab(ts.getSelectedTab()))
                + " selected");

    }
}