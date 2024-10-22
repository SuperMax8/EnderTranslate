package fr.supermax_8.endertranslate.core.utils;

import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.function.Consumer;

public class ComponentUtils {

    public static void forEachAllComponentsSeparated(Component component, Consumer<Component> consumer) {
        consumer.accept(component.children(List.of()));
        for (Component child : component.children())
            forEachAllComponentsSeparated(child, consumer);
    }

    public static void forEachAllComponents(Component component, Consumer<Component> consumer) {
        consumer.accept(component);
        for (Component child : component.children())
            forEachAllComponents(child, consumer);
    }

    public static LinkedList<Component> componentSeparatedList(Component component) {
        LinkedList<Component> components = new LinkedList<>();
        forEachAllComponentsSeparated(component, components::add);
        return components;
    }

    public static Component mergeComponents(List<Component> components) {
        if (components.isEmpty()) return null;
        Iterator<Component> itr = components.iterator();
        List<Component> childrens = new ArrayList<>();
        Component first = itr.next();
        while (itr.hasNext())
            childrens.add(itr.next());
        return first.children(childrens);
    }

}