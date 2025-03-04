package com.paragon.client.managers

import com.paragon.Paragon
import com.paragon.api.setting.Bind
import com.paragon.api.setting.Bind.Device
import com.paragon.api.setting.Setting
import com.paragon.client.managers.alt.Alt
import com.paragon.client.managers.social.Player
import com.paragon.client.managers.social.Relationship
import com.paragon.client.systems.module.hud.HUDModule
import org.apache.commons.io.FileUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.awt.Color
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * @author SooStrator1136
 */
class StorageManager {

    private val mainFolder = File("paragon")
    private val configFolder = File("paragon${File.separator}configs${File.separator}")
    private val socialFolder = File("paragon${File.separator}social")

    @Throws(IOException::class, JSONException::class)
    private fun getJSON(file: File) = if (!Files.exists(file.toPath())) null else JSONObject(FileUtils.readFileToString(file, StandardCharsets.UTF_8))

    fun saveModules(configName: String) {
        // Create configs folder if it doesn't already exist
        if (!configFolder.exists()) {
            configFolder.mkdirs()
        }

        val saveConfigFolder = File("paragon${File.separator}configs${File.separator}$configName")

        // Create the folder if it doesn't exist
        if (!saveConfigFolder.exists()) {
            saveConfigFolder.mkdirs()
        }

        Paragon.INSTANCE.moduleManager.modules.forEach { module ->
            try {
                val jsonObject = JSONObject()

                jsonObject.put("enabled", module.isEnabled)

                if (module is HUDModule) {
                    jsonObject.put("x", module.x)
                    jsonObject.put("y", module.y)
                }

                for (setting in module.getSettings()) {
                    if (setting.value is Color) {
                        val color = setting.value as Color
                        jsonObject.put(
                            setting.name,
                            color.red.toString() + ":" + color.green + ":" + color.blue + ":" + setting.alpha + ":" + setting.isRainbow + ":" + setting.rainbowSpeed + ":" + setting.rainbowSaturation + ":" + setting.isSync
                        )
                    } else if (setting.value is Bind) {
                        val bind = setting.value as Bind
                        jsonObject.put(
                            setting.name,
                            bind.buttonCode.toString() + ":" + bind.device
                        )
                    } else {
                        jsonObject.put(setting.name, setting.value)
                    }

                    if (setting.subsettings.isNotEmpty()) {
                        for (subSetting in setting.subsettings) {
                            val subSettingName = subSetting.parentSetting?.name + " " + subSetting.name
                            if (subSetting.value is Color) {
                                val color = subSetting.value as Color
                                jsonObject.put(
                                    subSettingName,
                                    color.red.toString() + ":" + color.green + ":" + color.blue + ":" + subSetting.alpha + ":" + subSetting.isRainbow + ":" + subSetting.rainbowSpeed + ":" + subSetting.rainbowSaturation + ":" + subSetting.isSync
                                )
                            } else if (subSetting.value is Bind) {
                                val bind = subSetting.value as Bind
                                jsonObject.put(
                                    subSettingName,
                                    bind.buttonCode.toString() + ":" + bind.device
                                )
                            } else {
                                jsonObject.put(subSettingName, subSetting.value)
                            }
                        }
                    }
                }

                // Write to file
                FileWriter(File(saveConfigFolder, module.name + ".json")).use {
                    it.write(jsonObject.toString(4))
                    it.flush()
                }
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun loadModules(configName: String) {
        val loadFolder = File("paragon${File.separator}configs${File.separator}$configName")

        if (!loadFolder.exists()) {
            loadFolder.mkdirs()
            return
        }

        Paragon.INSTANCE.moduleManager.modules.forEach { module ->
            val moduleJSON = getJSON(File(loadFolder, module.name + ".json")) ?: return@forEach

            try {
                if (moduleJSON.has("x") && moduleJSON.has("y")) {
                    module as HUDModule
                    module.x = moduleJSON.getInt("x").toFloat()
                    module.y = moduleJSON.getInt("y").toFloat()
                }

                fun loadSetting(setting: Setting<*>, isSub: Boolean) {
                    runCatching {
                        val settingName = if (isSub) setting.parentSetting?.name + " " + setting.name else setting.name

                        if (setting.value is Boolean) {
                            (setting as Setting<Boolean?>).setValue(moduleJSON.getBoolean(settingName))
                        } else if (setting.value is Bind) {
                            val bind = setting.value as Bind
                            val parts = moduleJSON.getString(settingName).split(":".toRegex()).toTypedArray()

                            bind.buttonCode = parts[0].toInt()
                            bind.device = java.lang.Enum.valueOf(
                                Device::class.java,
                                parts[1]
                            )
                        } else if (setting.value is Float) {
                            (setting as Setting<Float?>).setValue(moduleJSON.getFloat(settingName))
                        } else if (setting.value is Double) {
                            (setting as Setting<Double?>).setValue(moduleJSON.getDouble(settingName))
                        } else if (setting.value is Enum<*>) {
                            val enum = setting.value as Enum<*>
                            val value = java.lang.Enum.valueOf(
                                enum::class.java,
                                moduleJSON.getString(settingName)
                            )

                            run breakLoop@{
                                enum::class.java.enumConstants.forEachIndexed { index, enumValue ->
                                    if (enumValue.name == value.name) {
                                        setting.index = index
                                        return@breakLoop
                                    }
                                }
                            }

                            (setting as Setting<Enum<*>>).setValue(value)
                        } else if (setting.value is Color) {
                            val values = moduleJSON.getString(settingName).split(":".toRegex()).toTypedArray()

                            val color = Color(
                                values[0].toInt() / 255f,
                                values[1].toInt() / 255f,
                                values[2].toInt() / 255f,
                                values[3].toFloat() / 255f
                            )

                            setting.alpha = values[3].toFloat()
                            setting.isRainbow = java.lang.Boolean.parseBoolean(values[4])
                            setting.rainbowSpeed = values[5].toFloat()
                            setting.rainbowSaturation = values[6].toFloat()
                            setting.isSync = java.lang.Boolean.parseBoolean(values[7])
                            (setting as Setting<Color?>).setValue(color)
                        }
                    }
                }

                module.getSettings().forEach {
                    loadSetting(it, false)
                    it.subsettings.forEach { subSetting ->
                        loadSetting(subSetting, true)
                    }
                }

                if (moduleJSON.getBoolean("enabled") == !module.isEnabled) {
                    module.toggle()
                }
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }
        }
    }

    /**
     * Saves the social interactions
     */
    fun saveSocial() {
        if (!socialFolder.exists()) {
            socialFolder.mkdir() //Create friends folder if it doesn't already exist
        }

        val file = File("paragon${File.separator}social${File.separator}social_interactions.json")
        file.createNewFile()

        FileWriter(file).use { writer ->
            val jsonObject = JSONObject()
            val array = JSONArray()

            Paragon.INSTANCE.socialManager.players.forEach {
                array.put(it.name + ":" + it.relationship.name) //Put the player's info in the array - name:relationship
            }

            jsonObject.putOpt("acquaintances", array) // Add array to json object

            writer.write(jsonObject.toString(4))
            writer.flush()
        }
    }

    /**
     * Loads the social interactions
     */
    fun loadSocial() {
        if (!socialFolder.exists()) {
            socialFolder.mkdirs() //Create friends folder if it doesn't already exist
            return //If there is no folder there won't be anything to load either
        }

        runCatching {
            val json =
                getJSON(File("paragon${File.separator}social${File.separator}social_interactions.json")) ?: return

            val array = json.getJSONArray("acquaintances")

            //For every value in array, create add a player to the SocialManager
            for (i in 0 until array.length()) {
                val info: Array<String> = array.get(i).toString().split(":".toRegex()).toTypedArray()
                Paragon.INSTANCE.socialManager.addPlayer(Player(info[0], Relationship.valueOf(info[1])))
            }
        }
    }

    fun saveAlts() {
        if (!mainFolder.exists()) {
            mainFolder.mkdirs() //Create main folder if it doesn't already exist
        }

        val file = File("paragon${File.separator}alts.json")
        file.createNewFile()

        val jsonObject = JSONObject()

        FileWriter(file).use { writer ->
            val array = JSONArray()

            Paragon.INSTANCE.altManager.alts.forEach {
                array.put(it.email + ":" + it.password) //Put the player's info in the array - email:password
            }

            jsonObject.putOpt("alts", array)

            writer.write(jsonObject.toString(4))
            writer.flush()
        }
    }

    fun loadAlts() {
        if (!mainFolder.exists()) {
            mainFolder.mkdirs()
            return
        }

        runCatching {
            val json = getJSON(File("paragon${File.separator}alts.json")) ?: return
            val alts = json.getJSONArray("alts")

            //For every entry in the array, add a new alt
            for (i in 0 until alts.length()) {
                val info = alts[i].toString().split(":".toRegex()).toTypedArray()
                Paragon.INSTANCE.altManager.addAlt(Alt(info[0], info[1]))
            }
        }
    }

    fun saveOther() {
        if (!mainFolder.exists()) {
            mainFolder.mkdirs()
        }

        runCatching {
            val file = File("paragon${File.separator}client.json")
            file.createNewFile()

            FileWriter(file).use { writer ->
                val jsonObject = JSONObject()

                jsonObject.put("mainmenu", Paragon.INSTANCE.isParagonMainMenu)

                var prefixes = ""
                for (prefix in Paragon.INSTANCE.commandManager.commonPrefixes) {
                    prefixes += "$prefix "
                }
                jsonObject.put("ignored_prefixes", prefixes)

                writer.write(jsonObject.toString(4))
                writer.flush()
            }
        }
    }

    fun loadOther() {
        if (!mainFolder.exists()) {
            mainFolder.mkdirs()
            return
        }

        runCatching {
            val jsonObject = getJSON(File("paragon${File.separator}client.json")) ?: return

            Paragon.INSTANCE.isParagonMainMenu = jsonObject.getBoolean("mainmenu")

            if (jsonObject.has("ignored_prefixes")) {
                for (prefix in jsonObject.getString("ignored_prefixes").toString().split(" ".toRegex())) {
                    Paragon.INSTANCE.commandManager.commonPrefixes.add(prefix)
                }
            }
        }
    }

}