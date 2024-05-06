package fr.supermax_8.endertranslate.core;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.nbt.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientSettings;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import fr.supermax_8.endertranslate.core.player.TranslatePlayerManager;
import fr.supermax_8.endertranslate.core.translation.Translation;
import fr.supermax_8.endertranslate.core.translation.TranslationManager;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PacketEventsHandler {

    public PacketEventsHandler() {
        initPackets();
    }

    private void initPackets() {
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerAbstract() {
            @Override
            public void onPacketReceive(PacketReceiveEvent e) {
                if (e.getPacketType() == PacketType.Configuration.Client.CLIENT_SETTINGS) {
                    WrapperConfigClientSettings packet = new WrapperConfigClientSettings(e);
                    EnderTranslate.log("Player local: " + packet.getLocale());
                }
            }

            public void onPacketSend(PacketSendEvent e) {
                PacketTypeCommon packetType = e.getPacketType();
                if (!(packetType instanceof PacketType.Play.Server type)) return;
                switch (type) {
                    case SYSTEM_CHAT_MESSAGE -> {
                        WrapperPlayServerSystemChatMessage packet = new WrapperPlayServerSystemChatMessage(e);
                        applyTranslateOnPacketSend(e, packet::getMessage, packet::setMessage);
                    }
                    case ACTION_BAR -> {
                        WrapperPlayServerActionBar packet = new WrapperPlayServerActionBar(e);
                        applyTranslateOnPacketSend(e, packet::getActionBarText, packet::setActionBarText);
                    }
                    case TITLE -> {
                        WrapperPlayServerTitle packet = new WrapperPlayServerTitle(e);
                        applyTranslateOnPacketSend(e, packet::getTitle, packet::setTitle);
                        applyTranslateOnPacketSend(e, packet::getSubtitle, packet::setSubtitle);
                    }
                    case SET_TITLE_TEXT -> {
                        WrapperPlayServerSetTitleText packet = new WrapperPlayServerSetTitleText(e);
                        applyTranslateOnPacketSend(e, packet::getTitle, packet::setTitle);
                    }
                    case SET_TITLE_SUBTITLE -> {
                        WrapperPlayServerSetTitleSubtitle packet = new WrapperPlayServerSetTitleSubtitle(e);
                        applyTranslateOnPacketSend(e, packet::getSubtitle, packet::setSubtitle);
                    }
                    case OPEN_WINDOW -> {
                        WrapperPlayServerOpenWindow packet = new WrapperPlayServerOpenWindow(e);
                        applyTranslateOnPacketSend(e, packet::getTitle, packet::setTitle);
                    }
                    case ENTITY_METADATA -> {
                        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(e);
                        for (EntityData data : packet.getEntityMetadata()) {
                            if (data.getType() != EntityDataTypes.OPTIONAL_ADV_COMPONENT) continue;
                            Optional<Component> name = (Optional<Component>) data.getValue();
                            if (name.isPresent())
                                applyTranslateOnPacketSend(e, name::get, comp -> data.setValue(Optional.of(comp)));
                            return;
                        }
                    }
                    case WINDOW_ITEMS -> {
                        WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(e);
                        applyTranslateOnItemStacks(e, packet.getItems());
                    }
                    case SET_SLOT -> {
                        WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(e);
                        applyTranslateOnItemStack(e, packet.getItem());
                    }
                    case DISCONNECT -> {
                        WrapperPlayServerDisconnect packet = new WrapperPlayServerDisconnect(e);
                        applyTranslateOnPacketSend(e, packet::getReason, packet::setReason);
                    }
                    case BOSS_BAR -> {
                        WrapperPlayServerBossBar packet = new WrapperPlayServerBossBar(e);
                        if (packet.getAction() instanceof WrapperPlayServerBossBar.UpdateTitleAction titleAction) {
                            applyTranslateOnPacketSend(e, () -> titleAction.title, comp -> titleAction.title = comp);
                        }
                    }
                    case PLAYER_LIST_HEADER_AND_FOOTER -> {
                        WrapperPlayServerPlayerListHeaderAndFooter packet = new WrapperPlayServerPlayerListHeaderAndFooter(e);
                        applyTranslateOnPacketSend(e, packet::getHeader, packet::setHeader);
                        applyTranslateOnPacketSend(e, packet::getFooter, packet::setFooter);
                    }
                    case DISPLAY_SCOREBOARD -> {
                        WrapperPlayServerDisplayScoreboard packet = new WrapperPlayServerDisplayScoreboard(e);
                        applyTranslateOnPacketSendString(e, packet::getScoreName, packet::setScoreName);
                    }
                    case SCOREBOARD_OBJECTIVE -> {
                        WrapperPlayServerScoreboardObjective packet = new WrapperPlayServerScoreboardObjective(e);
                        applyTranslateOnPacketSend(e, packet::getDisplayName, packet::setDisplayName);
                    }
                    case UPDATE_SCORE -> {
                        WrapperPlayServerUpdateScore packet = new WrapperPlayServerUpdateScore(e);
                        applyTranslateOnPacketSendString(e, packet::getObjectiveName, packet::setObjectiveName);
                    }
                }
            }
        });
    }


    public void applyTranslateOnItemStacks(PacketSendEvent e, Collection<ItemStack> stacks) {
        for (ItemStack stack : stacks)
            applyTranslateOnItemStack(e, stack);
    }

    public void applyTranslateOnItemStack(PacketSendEvent e, ItemStack stack) {
        boolean modified = false;
        NBTCompound nbt = stack.getNBT();
        if (nbt == null) return;
        Map<String, NBT> tags = nbt.getTags();

        UUID playerId = e.getUser().getUUID();

        // Translate book pages for signed books
        if (tags.containsKey("pages")) {
            NBTList<NBTString> newPages = null;
            NBTList<NBTString> pages = (NBTList<NBTString>) tags.get("pages");
            for (NBTString page : pages.getTags()) {
                String translated = applyTranslate(playerId, page.getValue());
                if (translated == null) continue;
                if (newPages == null) newPages = new NBTList<>(NBTType.STRING);
                newPages.addTag(new NBTString(translated));
            }
            if (newPages != null) nbt.setTag("pages", newPages);
            modified = true;
        }


        NBTCompound display = (NBTCompound) tags.get("display");
        if (display != null) {
            // Translate name
            Map<String, NBT> displayTags = display.getTags();
            if (displayTags.containsKey("Name")) {
                String name = ((NBTString) displayTags.get("Name")).getValue();
                String translated = applyTranslate(playerId, name);
                if (translated != null) {
                    modified = true;
                    display.setTag("Name", new NBTString(translated));
                }
            }

            // Translate lore
            if (displayTags.containsKey("Lore")) {
                NBTList<NBTString> lore = (NBTList<NBTString>) displayTags.get("Lore");
                NBTList<NBTString> newLore = null;
                for (NBTString line : lore.getTags()) {
                    String translated = applyTranslate(playerId, line.getValue());
                    if (translated == null) continue;
                    if (newLore == null) newLore = new NBTList<>(NBTType.STRING);
                    newLore.addTag(new NBTString(translated));
                }
                if (newLore != null) {
                    display.setTag("Lore", newLore);
                    modified = true;
                }
            }
        }

        if (modified) e.markForReEncode(true);
    }

    public void applyTranslateOnPacketSendString(PacketSendEvent e, Supplier<String> getMessage, Consumer<String> setMessage) {
        String translated = applyTranslate(e.getUser().getUUID(), getMessage.get());
        if (translated != null) {
            setMessage.accept(translated);
            e.markForReEncode(true);
        }
    }

    public void applyTranslateOnPacketSend(PacketSendEvent e, Supplier<Component> getMessage, Consumer<Component> setMessage) {
        Component toTranslate = getMessage.get();
        if (toTranslate == null) return;
        String message = AdventureSerializer.toLegacyFormat(toTranslate);
        String translated = applyTranslate(e.getUser().getUUID(), message);
        if (translated != null) {
            setMessage.accept(AdventureSerializer.fromLegacyFormat(translated));
            e.markForReEncode(true);
        }
    }

    public String applyTranslate(UUID playerId, String toTranslate) {
        String playerLanguage = TranslatePlayerManager.getInstance().getPlayerLanguage(playerId);
        StringBuilder sb = new StringBuilder(toTranslate);

        EnderTranslateConfig config = EnderTranslateConfig.getInstance();
        String startTag = config.getStartTag();
        String endTag = config.getEndTag();

        int startTagIndex = sb.indexOf(startTag);
        while (startTagIndex != -1) {
            int endTagIndex = sb.indexOf(endTag, startTagIndex);
            if (endTagIndex == -1) break;
            int startLangPlaceholderIndex = startTagIndex + startTag.length();
            String langPlaceholder = sb.substring(startLangPlaceholderIndex, endTagIndex);
            int startParamIndex = langPlaceholder.indexOf("{");
            String[] params = null;
            if (startParamIndex != -1) {
                params = langPlaceholder.substring(startParamIndex + 1, langPlaceholder.length() - 1).split(";");
                langPlaceholder = langPlaceholder.substring(0, startParamIndex);
            }

            String endValue;

            getEndValue:
            {
                Translation translation = TranslationManager.getInstance().getTranslation(langPlaceholder);
                if (translation == null) {
                    endValue = "TRANSLATION(id=" + langPlaceholder + ")_NOT_FOUND";
                    break getEndValue;
                }
                String translationValue = translation.getTranslation(playerLanguage);
                if (params == null) endValue = translationValue;
                else {
                    StringBuilder translationValueBuilder = new StringBuilder(translationValue);
                    int i = 0;
                    for (String param : params) {
                        int paramIndex = translationValueBuilder.indexOf("{" + i + "}");
                        if (paramIndex == -1) break;
                        translationValueBuilder.replace(paramIndex, paramIndex + 3, param);
                        i++;
                    }
                    endValue = translationValueBuilder.toString();
                }
            }

            sb.replace(startTagIndex, endTagIndex + endTag.length(), endValue);

            startTagIndex = sb.indexOf(startTag);
        }

        String translatedMessage = sb.toString();
        return translatedMessage.equals(toTranslate) ? null : translatedMessage;
    }

}