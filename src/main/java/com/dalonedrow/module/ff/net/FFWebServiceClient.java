package com.dalonedrow.module.ff.net;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map.Entry;
import java.util.Set;

import com.dalonedrow.engine.systems.base.Interactive;
import com.dalonedrow.engine.systems.base.JOGLErrorHandler;
import com.dalonedrow.module.ff.constants.FFEquipmentElements;
import com.dalonedrow.module.ff.graph.FFMapNode;
import com.dalonedrow.module.ff.graph.FFRoomNode;
import com.dalonedrow.module.ff.graph.FFWorldMap;
import com.dalonedrow.module.ff.rpg.FFCommand;
import com.dalonedrow.module.ff.rpg.FFInteractiveObject;
import com.dalonedrow.module.ff.rpg.FFItem;
import com.dalonedrow.module.ff.rpg.FFNpc;
import com.dalonedrow.module.ff.rpg.FFScriptable;
import com.dalonedrow.module.ff.systems.FFInteractive;
import com.dalonedrow.net.WebServiceClient;
import com.dalonedrow.pooled.PooledException;
import com.dalonedrow.pooled.PooledStringBuilder;
import com.dalonedrow.pooled.StringBuilderPool;
import com.dalonedrow.rpg.base.flyweights.Attribute;
import com.dalonedrow.rpg.base.flyweights.EquipmentItemModifier;
import com.dalonedrow.rpg.base.flyweights.ErrorMessage;
import com.dalonedrow.rpg.base.flyweights.RPGException;
import com.dalonedrow.rpg.base.systems.Script;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * @author drau
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class FFWebServiceClient extends WebServiceClient {
    /** the singleton instance of {@link FFWebServiceClient}. */
    private static FFWebServiceClient instance;
    /**
     * Gets the singleton instance.
     * @return {@link FFWebServiceClient}
     */
    public static FFWebServiceClient getInstance() {
        if (instance == null) {
            try {
                instance = new FFWebServiceClient();
            } catch (IOException e) {
                JOGLErrorHandler.getInstance().fatalError(e);
            }
        }
        return instance;
    }
    /**
     * Hidden constructor.
     * @throws IOException if an error occurs
     */
    protected FFWebServiceClient() throws IOException {
        super(FFWebServiceClient.class.getClassLoader().getResourceAsStream(
                "ff.properties"));
    }
    public void getAttributes() throws RPGException {
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        try {
            sb.append(super.getApiProperties().getProperty("endpoint"));
            sb.append(super.getApiProperties().getProperty("attributesApi"));
            String response = getResponse(sb.toString());
            Gson gson = new Gson();
            JsonArray results = gson.fromJson(response, JsonArray.class);
            for (int i = results.size() - 1; i >= 0; i--) {
                JsonObject obj = results.get(i).getAsJsonObject();
                Attribute attr = new Attribute(obj.get("code").getAsString(),
                        obj.get("description").getAsString(),
                        obj.get("name").getAsString());
            }
        } catch (PooledException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        sb.returnToPool();
        sb = null;
    }
    public JsonArray getEquipmentElements() throws RPGException {
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        JsonArray results = null;
        try {
            sb.append(super.getApiProperties().getProperty("endpoint"));
            sb.append(super.getApiProperties().getProperty("equipElementApi"));
            String response = getResponse(sb.toString());
            Gson gson = new Gson();
            results = gson.fromJson(response, JsonArray.class);
        } catch (PooledException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        sb.returnToPool();
        sb = null;
        return results;
    }
    public JsonArray getEquipmentSlots() throws RPGException {
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        JsonArray results = null;
        try {
            sb.append(super.getApiProperties().getProperty("endpoint"));
            sb.append(super.getApiProperties().getProperty("equipSlotsApi"));
            String response = getResponse(sb.toString());
            Gson gson = new Gson();
            results = gson.fromJson(response, JsonArray.class);
        } catch (PooledException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        sb.returnToPool();
        sb = null;
        return results;
    }
    public EquipmentItemModifier getModifierByCode(final String name)
            throws RPGException {
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        EquipmentItemModifier modifier;
        try {
            sb.append(super.getApiProperties().getProperty("endpoint"));
            sb.append(super.getApiProperties().getProperty("equipModifersApi"));
            sb.append("code/");
            sb.append(name.replaceAll(" ", "%20"));
            String response = getResponse(sb.toString());
            Gson gson = new Gson();
            JsonArray results = gson.fromJson(response, JsonArray.class);
            modifier = gson.fromJson(
                    results.get(0), EquipmentItemModifier.class);
        } catch (JsonSyntaxException | PooledException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        sb.returnToPool();
        sb = null;
        return modifier;
    }
    /**
     * Loads an item by its name.
     * @param itemName the item's name
     * @return {@link FFInteractiveObject}
     * @throws RPGException if an error occurs
     */
    public FFInteractiveObject loadItem(final String itemName)
            throws RPGException {
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        FFInteractiveObject io =
                ((FFInteractive) Interactive.getInstance()).newItem();
        try {
            FFItem itemData = new FFItem();
            io.setItemData(itemData);
            sb.append(super.getApiProperties().getProperty("endpoint"));
            sb.append(super.getApiProperties().getProperty("itemApi"));
            sb.append("name/");
            sb.append(itemName.replaceAll(" ", "%20"));
            String response = getResponse(sb.toString());
            sb.setLength(0);
            Gson gson = new Gson();
            JsonObject obj = gson.fromJson(
                    response, JsonArray.class).get(0).getAsJsonObject();
            // *************************************************
            // weight
            // *************************************************
            if (obj.has("weight")) {
                itemData.setWeight(obj.get("weight").getAsFloat());
            } else {
                itemData.setWeight(0);
            }
            // *************************************************
            // stack_size
            // *************************************************
            if (obj.has("stack_size")) {
                itemData.setStackSize(obj.get("stack_size").getAsInt());
            } else {
                itemData.setStackSize(1);
            }
            // *************************************************
            // name
            // *************************************************
            itemData.setItemName(obj.get("name").getAsString());
            // *************************************************
            // title
            // *************************************************
            itemData.setTitle(obj.get("title").getAsString());
            // *************************************************
            // max_owned
            // *************************************************
            if (obj.has("max_owned")) {
                itemData.setMaxOwned(obj.get("max_owned").getAsInt());
            } else {
                itemData.setMaxOwned(1);
            }
            // *************************************************
            // description
            // *************************************************
            itemData.setDescription(obj.get("description").getAsString());
            // *************************************************
            // types
            // *************************************************
            JsonArray types = obj.get("types").getAsJsonArray();
            for (int i = types.size() - 1; i >= 0; i--) {
                itemData.ARX_EQUIPMENT_SetObjectType(
                        types.get(i).getAsJsonObject().get("flag").getAsInt(),
                        true);
            }
            // *************************************************
            // modifiers
            // *************************************************
            JsonObject modifiers = obj.get("modifiers").getAsJsonObject();
            Set<Entry<String, JsonElement>> entries = modifiers.entrySet();
            for (Entry<String, JsonElement> entry : entries) {
                int elementIndex =
                        FFEquipmentElements.valueOf(entry.getKey()).getIndex();
                itemData.getEquipitem().getElement(elementIndex).set(
                        getModifierByCode(entry.getValue().getAsString()));
            }
            // *************************************************
            // internal_script
            // *************************************************
            sb.append("com.dalonedrow.module.ff.scripts.items.");
            sb.append(obj.get("internal_script").getAsString());
            Class internalScript = Class.forName(sb.toString());
            try {
                Constructor con = internalScript
                        .getConstructor(FFInteractiveObject.class);
                FFScriptable script = (FFScriptable) con.newInstance(io);
                io.setScript(script);
            } catch (NoSuchMethodException | SecurityException
                    | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            sb.setLength(0);
        } catch (PooledException | ClassNotFoundException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        sb.returnToPool();
        sb = null;
        Script.getInstance().sendInitScriptEvent(io);
        return io;
    }
    /**
     * Loads the world map.
     * @throws RPGException if an error occurs
     */
    public void loadMap() throws RPGException {
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        try {
            sb.append(super.getApiProperties().getProperty("endpoint"));
            sb.append(super.getApiProperties().getProperty("mapApi"));
            String response = getResponse(sb.toString());
            sb.returnToPool();
            Gson gson = new Gson();
            JsonArray results = gson.fromJson(response, JsonArray.class);
            int index = 0;
            for (int i = results.size() - 1; i >= 0; i--) {
                JsonObject obj = results.get(i).getAsJsonObject();
                FFMapNode node;
                boolean isMain = false;
                int roomNumber;
                int x = 0, y = 0;
                if (obj.has("x")) {
                    x = obj.get("x").getAsInt();
                }
                if (obj.has("y")) {
                    y = obj.get("y").getAsInt();
                }
                if (obj.has("terrain")
                        && obj.getAsJsonObject("terrain").has("name")) {
                    node = new FFMapNode(
                            obj.getAsJsonObject("terrain").get("name")
                                    .getAsString(),
                            index++, x, y);
                } else {
                    throw new RPGException(ErrorMessage.BAD_PARAMETERS,
                            "Node " + x + "," + y + " has no terrain data");
                }
                if (obj.has("is_main_node")) {
                    isMain = obj.get("is_main_node").getAsBoolean();
                }
                if (obj.has("room_number")) {
                    roomNumber = obj.get("room_number").getAsInt();
                } else {
                    throw new RPGException(ErrorMessage.BAD_PARAMETERS,
                            "Node " + x + "," + y + " has no room number");
                }
                FFWorldMap.getInstance().addNode(node, roomNumber, isMain);
            }
        } catch (PooledException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        sb = null;
    }
    /**
     * Loads an item by its name.
     * @param npcName the item's name
     * @return {@link FFInteractiveObject}
     * @throws RPGException if an error occurs
     */
    public FFInteractiveObject loadNPC(final String npcName)
            throws RPGException {
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        FFInteractiveObject io =
                ((FFInteractive) Interactive.getInstance()).newNPC();
        try {
            FFNpc npcData = new FFNpc();
            io.setNPCData(npcData);
            sb.append(super.getApiProperties().getProperty("endpoint"));
            sb.append(super.getApiProperties().getProperty("npcApi"));
            sb.append("name/");
            sb.append(npcName.replaceAll(" ", "%20"));
            String response = getResponse(sb.toString());
            sb.setLength(0);
            Gson gson = new Gson();
            JsonObject obj = gson.fromJson(
                    response, JsonArray.class).get(0).getAsJsonObject();
            // *************************************************
            // title
            // *************************************************
            if (obj.has("title")) {
                npcData.setTitle(obj.get("title").getAsString());
            } else {
                npcData.setTitle("");
            }
            // *************************************************
            // name
            // *************************************************
            if (obj.has("name")) {
                npcData.setName(obj.get("name").getAsString());
            } else {
                npcData.setName("");
            }
            // *************************************************
            // gender
            // *************************************************
            if (obj.has("gender")) {
                String genName =
                        obj.getAsJsonObject("gender").get("name").getAsString();
                if (genName.equalsIgnoreCase("male")) {
                    npcData.setGender(0);
                } else {
                    npcData.setGender(1);
                }
                genName = null;
            }
            // *************************************************
            // attributes
            // *************************************************
            if (obj.has("attributes")) {
                JsonObject attributes = obj.get("attributes").getAsJsonObject();
                Set<Entry<String, JsonElement>> entries = attributes.entrySet();
                for (Entry<String, JsonElement> entry : entries) {
                    npcData.setBaseAttributeScore(
                            entry.getKey(), entry.getValue().getAsInt());
                }
                attributes = null;
                entries = null;
            }
            // *************************************************
            // internal_script
            // *************************************************
            sb.append("com.dalonedrow.module.ff.scripts.npcs.");
            sb.append(obj.get("internal_script").getAsString());
            Class internalScript = Class.forName(sb.toString());
            try {
                Constructor con = internalScript
                        .getConstructor(FFInteractiveObject.class);
                FFScriptable script = (FFScriptable) con.newInstance(io);
                io.setScript(script);
            } catch (NoSuchMethodException | SecurityException
                    | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException e) {
                throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
            }
            sb.setLength(0);
            // *************************************************
            // weapon
            // *************************************************
            if (obj.has("weapon")) {
                FFInteractiveObject wpnIO =
                        loadItem(obj.get("weapon").getAsString());
                wpnIO.getItemData().ARX_EQUIPMENT_Equip(io);
            }
        } catch (PooledException | ClassNotFoundException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        sb.returnToPool();
        sb = null;
        Script.getInstance().sendInitScriptEvent(io);
        return io;
    }
    /**
     * Loads data for a specific room.
     * @param room the room
     * @throws RPGException if an error occurs
     */
    public void loadRoomData(final FFRoomNode room) throws RPGException {
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        try {
            sb.append(super.getApiProperties().getProperty("endpoint"));
            sb.append(super.getApiProperties().getProperty("roomApi"));
            sb.append("/code/");
            sb.append(room.getId());
            String response = getResponse(sb.toString());
            sb.returnToPool();
            Gson gson = new Gson();
            JsonArray results = gson.fromJson(response, JsonArray.class);
            for (int i = results.size() - 1; i >= 0; i--) {
                JsonObject obj = results.get(i).getAsJsonObject();
                if (obj.has("commands")) {
                    JsonArray commands = obj.getAsJsonArray("commands");
                    for (int j = commands.size() - 1; j >= 0; j--) {
                        JsonObject command = commands.get(j).getAsJsonObject();
                        if (command.has("name")) {
                            room.addCommand(FFCommand.valueOf(
                                    command.get("name").getAsString()));
                        }
                        command = null;
                    }
                    commands = null;
                }
                System.out.println("get text for room "+room.getId());
                room.setDisplayText(loadText(Integer.toString(room.getId())));
                obj = null;
            }
            response = null;
            gson = null;
            results = null;
        } catch (PooledException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        sb = null;
    }
    /**
     * Loads the game text with a call to the web service.
     * @param section the text section
     * @return {@link String}
     * @throws RPGException
     */
    public String loadText(final String section) throws RPGException {
        String s;
        PooledStringBuilder sb =
                StringBuilderPool.getInstance().getStringBuilder();
        try {
            sb.append(super.getApiProperties().getProperty("endpoint"));
            sb.append(super.getApiProperties().getProperty("textApi"));
            sb.append("name/");
            sb.append(section.replaceAll(" ", "%20"));
            String response = getResponse(sb.toString());
            sb.returnToPool();
            Gson gson = new Gson();
            JsonObject obj = gson.fromJson(
                    response, JsonArray.class).get(0).getAsJsonObject();
            // *************************************************
            // text
            // *************************************************
            if (obj.has("text")) {
                s = obj.get("text").getAsString();
            } else {
                throw new RPGException(ErrorMessage.INVALID_DATA_FORMAT,
                        "Text has no content");
            }
        } catch (PooledException e) {
            throw new RPGException(ErrorMessage.INTERNAL_ERROR, e);
        }
        sb = null;
        return s;
    }
}
