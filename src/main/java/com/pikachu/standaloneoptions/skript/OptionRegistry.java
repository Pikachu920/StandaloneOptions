package com.pikachu.standaloneoptions.skript;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.StringUtils;
import com.pikachu.standaloneoptions.StubBukkitEvent;
import org.bukkit.event.Event;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

public class OptionRegistry extends SelfRegisteringSkriptEvent {

    private final static Field NODE_LIST;
    private final static Field NODE_KEY;
    private static HashMap<String, String> currentOptions = new HashMap<>();

    static {

        Field _NODE_LIST = null;
        try {
            _NODE_LIST = SectionNode.class.getDeclaredField("nodes");
            _NODE_LIST.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        NODE_LIST = _NODE_LIST;

    }

    static {

        Field _NODE_KEY = null;
        try {
            _NODE_KEY = Node.class.getDeclaredField("key");
            _NODE_KEY.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        NODE_KEY = _NODE_KEY;

    }

    static {

        Skript.registerEvent("Standalone Options", OptionRegistry.class, StubBukkitEvent.class,
                "standalone options");

    }

    /*
    * Could be done without recursion, but that's some
    * annoying testing for something that is just a proof of concept anyways
    */
    public void replaceOptions(SectionNode sectionNode) {
        try {
            for (Node node : (ArrayList<Node>) NODE_LIST.get(sectionNode)) {
                String replaced = StringUtils.replaceAll(node.getKey(), "\\|(.+?)\\|", matcher -> {
                    String toReplace = currentOptions.get(matcher.group(1));
                    if (toReplace == null) {
                        Skript.error("the option " + matcher.group(1) + " wasn't found");
                        return matcher.group();
                    }
                    return Matcher.quoteReplacement(toReplace);
                });
                NODE_KEY.set(node, replaced);
                if (node instanceof SectionNode) {
                    replaceOptions((SectionNode) node);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean init(final Literal<?>[] args, final int matchedPattern, final SkriptParser.ParseResult parser) {
        SectionNode scriptNode = ScriptLoader.currentScript.getMainNode();
        SectionNode optionNode = (SectionNode) SkriptLogger.getNode();
        try {
            currentOptions.clear();
            for (Node node : (ArrayList<Node>) NODE_LIST.get(optionNode)) {
                String[] option = node.getKey().split(": ");
                if (option.length == 2) {
                    currentOptions.put(option[0], option[1]);
                }
            }
            replaceOptions(scriptNode);
            nukeSectionNode(optionNode);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void afterParse(Config config) {
    }

    @Override
    public void register(Trigger t) {
    }

    @Override
    public void unregister(Trigger t) {
    }

    @Override
    public void unregisterAll() {
    }

    @Override
    public String toString(final Event e, final boolean debug) {
        return "standalone options";
    }

    public void nukeSectionNode(SectionNode sectionNode) {
        List<Node> nodes = new ArrayList<>();
        for (Iterator<Node> iterator = sectionNode.iterator(); iterator.hasNext(); ) {
            nodes.add(iterator.next());
        }
        for (Node n : nodes) {
            sectionNode.remove(n);
        }
    }

}
