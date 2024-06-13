package fr.supermax_8.endertranslate.core;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_1;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_19_3;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.nbt.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.score.FixedScoreFormat;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.github.retrooper.packetevents.wrapper.configuration.client.WrapperConfigClientSettings;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import fr.supermax_8.endertranslate.core.player.TranslatePlayerManager;
import fr.supermax_8.endertranslate.core.translation.Translation;
import fr.supermax_8.endertranslate.core.translation.TranslationManager;
import lombok.Getter;
import me.tofaa.entitylib.meta.EntityMeta;
import me.tofaa.entitylib.meta.Metadata;
import me.tofaa.entitylib.meta.display.TextDisplayMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PacketEventsHandler {

    @Getter
    private static PacketEventsHandler instance;
    private final ConcurrentHashMap<Integer, EntityType> entitiesType = new ConcurrentHashMap<>();
    @Getter
    private final ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, WrapperPlayServerEntityMetadata>> entitiesMetaData = new ConcurrentHashMap<>();

    public PacketEventsHandler() {
        instance = this;
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
                    case CHAT_MESSAGE -> {
                        WrapperPlayServerChatMessage packet = new WrapperPlayServerChatMessage(e);
                        ChatMessage message = packet.getMessage();
                        if (message instanceof ChatMessage_v1_19_3 cm) {
                            Optional<Component> unsigned = cm.getUnsignedChatContent();
                            if (unsigned.isEmpty())
                                applyTranslateOnPacketSend(e, message::getChatContent, message::setChatContent);
                            else applyTranslateOnPacketSend(e, unsigned::get, cm::setUnsignedChatContent);
                        } else if (message instanceof ChatMessage_v1_19_1 cm) {
                            Component unsigned = cm.getUnsignedChatContent();
                            if (unsigned == null)
                                applyTranslateOnPacketSend(e, message::getChatContent, message::setChatContent);
                            applyTranslateOnPacketSend(e, cm::getUnsignedChatContent, cm::setUnsignedChatContent);
                        } else if (message instanceof ChatMessage_v1_19 cm) {
                            Component unsigned = cm.getUnsignedChatContent();
                            if (unsigned == null)
                                applyTranslateOnPacketSend(e, message::getChatContent, message::setChatContent);
                            applyTranslateOnPacketSend(e, cm::getUnsignedChatContent, cm::setUnsignedChatContent);
                        } else
                            applyTranslateOnPacketSend(e, message::getChatContent, message::setChatContent);
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
                    case SPAWN_ENTITY -> {
                        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(e);
                        entitiesType.put(packet.getEntityId(), packet.getEntityType());
                    }
                    case DESTROY_ENTITIES -> {
                        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(e);
                        ConcurrentHashMap<Integer, WrapperPlayServerEntityMetadata> entityDataSent = entitiesMetaData.computeIfAbsent(e.getUser().getUUID(), k -> new ConcurrentHashMap<>());
                        for (int id : packet.getEntityIds()) {
                            entitiesType.remove(id);
                            entityDataSent.remove(id);
                        }
                    }
                    case ENTITY_METADATA -> {
                        try {
                            WrapperPlayServerEntityMetadata clone = new WrapperPlayServerEntityMetadata(e);
                            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(e);
                            int entityId = packet.getEntityId();
                            EntityType entityType = entitiesType.get(entityId);

                            Metadata meta = new Metadata(entityId);
                            meta.setMetaFromPacket(packet);
                            boolean translated = false;
                            if (entityType == EntityTypes.TEXT_DISPLAY) {
                                TextDisplayMeta textDisplayMeta = new TextDisplayMeta(entityId, meta);
                                translated = applyTranslateOnPacketSend(e, textDisplayMeta::getText, comp -> {
                                    textDisplayMeta.setText(comp);
                                    packet.setEntityMetadata(textDisplayMeta.createPacket().getEntityMetadata());
                                });
                            } else {
                                EntityMeta entityMeta = new EntityMeta(entityId, meta);
                                TextComponent name = (TextComponent) entityMeta.getCustomName();
                                if (name != null) {
                                    System.out.println("NAAAAME" + name.toString());
                                    translated = applyTranslateOnPacketSend(e, () -> name, comp -> {
                                        entityMeta.setCustomName(comp);
                                        packet.setEntityMetadata(entityMeta.createPacket().getEntityMetadata());
                                    });
                                }
                            }
                            if (translated) {
                                entitiesMetaData.computeIfAbsent(e.getUser().getUUID(), k -> new ConcurrentHashMap<>()).put(entityId, clone);
                                e.setLastUsedWrapper(packet);
                                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA ");
                            }
                        } catch (Exception ex) {
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
                        applyTranslateOnPacketSendString(e, packet::getName, packet::setName);
                        applyTranslateOnPacketSend(e, packet::getDisplayName, packet::setDisplayName);
                        applyTranslateOnScoreboardFormat(e, packet.getScoreFormat(), packet::setScoreFormat);
                    }
                    case RESET_SCORE -> {
                        WrapperPlayServerResetScore packet = new WrapperPlayServerResetScore(e);
                        applyTranslateOnPacketSendString(e, packet::getObjective, packet::setObjective);
                        applyTranslateOnPacketSendString(e, packet::getTargetName, packet::setTargetName);
                    }
                    case UPDATE_SCORE -> {
                        WrapperPlayServerUpdateScore packet = new WrapperPlayServerUpdateScore(e);
                        applyTranslateOnPacketSendString(e, packet::getObjectiveName, packet::setObjectiveName);
                        applyTranslateOnPacketSendString(e, packet::getEntityName, packet::setEntityName);
                        applyTranslateOnScoreboardFormat(e, packet.getScoreFormat(), packet::setScoreFormat);
                        applyTranslateOnPacketSend(e, packet::getEntityDisplayName, packet::setEntityDisplayName);
                    }
                    case TEAMS -> {
                        WrapperPlayServerTeams packet = new WrapperPlayServerTeams(e);
                        applyTranslateOnPacketSendString(e, packet::getTeamName, packet::setTeamName);
                        packet.getTeamInfo().ifPresent(scoreBoardTeamInfo -> {
                            applyTranslateOnPacketSend(e, scoreBoardTeamInfo::getDisplayName, scoreBoardTeamInfo::setDisplayName);
                            applyTranslateOnPacketSend(e, scoreBoardTeamInfo::getPrefix, scoreBoardTeamInfo::setPrefix);
                            applyTranslateOnPacketSend(e, scoreBoardTeamInfo::getSuffix, scoreBoardTeamInfo::setSuffix);
                        });
                    }
                }
            }
        });
    }

    public void resendEntityMetaPackets(Object playerObj) {
        User user = PacketEvents.getAPI().getPlayerManager().getUser(playerObj);
        ConcurrentHashMap<Integer, WrapperPlayServerEntityMetadata> entityMetadataSent = entitiesMetaData.computeIfAbsent(user.getUUID(), k -> new ConcurrentHashMap<>());
        entityMetadataSent.values().forEach(user::sendPacket);
    }

    public void applyTranslateOnScoreboardFormat(PacketSendEvent e, ScoreFormat format, Consumer<ScoreFormat> setFormat) {
        if (!(format instanceof FixedScoreFormat fixedScoreFormat)) return;
        applyTranslateOnPacketSend(e, fixedScoreFormat::getValue, component ->
                setFormat.accept(new FixedScoreFormat(component)));
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

    public boolean applyTranslateOnPacketSend(PacketSendEvent e, Supplier<Component> getMessage, Consumer<Component> setMessage) {
        Component toTranslate = getMessage.get();
        if (toTranslate == null) return false;
        String message = AdventureSerializer.toJson(toTranslate);
        String translated = applyTranslate(e.getUser().getUUID(), message);
        if (translated != null) {
            setMessage.accept(AdventureSerializer.parseComponent(translated));
            e.markForReEncode(true);
            return true;
        }
        return false;
    }

    public String applyTranslate(UUID playerId, String toTranslate) {
        String playerLanguage = TranslatePlayerManager.getInstance().getPlayerLanguage(playerId);
        return applyTranslate(playerLanguage, toTranslate);
    }

    public String applyTranslate(String playerLanguage, String toTranslate) {
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
            String endValue = translatePlaceholder(langPlaceholder, playerLanguage);

            sb.replace(startTagIndex, endTagIndex + endTag.length(), endValue);
            startTagIndex = sb.indexOf(startTag);
        }

        String translatedMessage = sb.toString();
        return translatedMessage.equals(toTranslate) ? null : translatedMessage;
    }

    /**
     * Translate placeholder with params
     * @param langPlaceholder the placeholder e.g hello_chat{aaaa;bb}
     * @param playerLanguage
     * @return
     */
    public String translatePlaceholder(String langPlaceholder, String playerLanguage) {
        TranslationManager translationManager = TranslationManager.getInstance();

        // Load params
        int startParamIndex = langPlaceholder.indexOf("{");
        String[] params = null;
        if (startParamIndex != -1) {
            params = langPlaceholder.substring(startParamIndex + 1, langPlaceholder.length() - 1).split(";");
            /*System.out.println("PARAMS " + params);
            for (int i = 0; i < params.length; i++) {
                System.out.println("PAAAAARAM " + params[i]);
                String translate = applyTranslate(playerLanguage, params[i]);
                if (translate != null) params[i] = translate;
            }*/
            langPlaceholder = langPlaceholder.substring(0, startParamIndex);
        }

        String endValue;

        getEndValue:
        {
            Translation translation = translationManager.getTranslation(langPlaceholder);
            String translationValue;
            if (translation == null || (translationValue = translation.getTranslation(playerLanguage)) == null) {
                endValue = "TRANSLATION(id=" + langPlaceholder + ")_NOT_FOUND";
                break getEndValue;
            }
            if (params == null) endValue = translationValue;
            else {
                StringBuilder translationValueBuilder = new StringBuilder(translationValue);
                int i = 0;
                for (String param : params) {
                    int paramIndex = translationValueBuilder.indexOf("{" + i + "}");
                    if (paramIndex == -1) break;
                    Translation paramTranslation = translationManager.getTranslation(param);
                    translationValueBuilder.replace(paramIndex, paramIndex + 3, paramTranslation == null ? param : paramTranslation.getTranslation(playerLanguage));
                    i++;
                }
                endValue = translationValueBuilder.toString();
            }
        }

        return endValue;
    }

}