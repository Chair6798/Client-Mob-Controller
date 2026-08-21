package org.coolchair.client_mob_controller.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.sun.jdi.connect.Connector;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.entity.EntityType;
import org.coolchair.client_mob_controller.Client_mob_controller;

import java.util.Objects;

class Util
{
    static public void Teleport(Entity e, Minecraft client)
    {
        if(client.getConnection()==null)
        {
            return;
        }
        String tpCmd = String.format("tp %s @s", e.getUUID().toString());
        client.getConnection().sendCommand(tpCmd);
    }
    static public void Teleport(Entity e, Minecraft client, Position pos)
    {
        Teleport(e, client, pos.x(), pos.y(), pos.z());
    }
    static public void Teleport(Entity e, Minecraft client, double x, double y, double z)
    {
        if(client.getConnection()==null)
        {
            return;
        }
        String tpCmd = String.format("tp %s %s %s %s", e.getUUID().toString(), String.valueOf(x), String.valueOf(y), String.valueOf(z));
        client.getConnection().sendCommand(tpCmd);
    }
    static public void Kill(Entity e, Minecraft client)
    {
        if(client.getConnection()==null)
        {
            return;
        }
        String killCmd = String.format("kill %s", e.getUUID().toString());
        client.getConnection().sendCommand(killCmd);
    }
}
public class Client_mob_controllerClient implements ClientModInitializer {

    public Client_mob_controller instance;
    private Minecraft client;
    private Entity currentControll;
    @Override
    public void onInitializeClient() {
        client=Minecraft.getInstance();
        ClientTickEvents.END_LEVEL_TICK.register(c->{
            if(client.level==null||client.player==null)
            {
                return;
            }
            if(currentControll==null)
            {
                for(Entity e : client.level.entitiesForRendering())
                {
                    if(e.getCustomName()==null||!e.isAlive())
                    {
                        continue;
                    }
                    if(Objects.equals(e.getCustomName().getString(), client.player.getName().getString() + "_"))
                    {
                        currentControll=e;
                    }
                }

            }
            if(currentControll==null||!currentControll.isAlive()||client.getConnection()==null)
            {
                return;
            }
            Util.Teleport(currentControll, client);


        });
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("be")
                .then(ClientCommands.argument("type", StringArgumentType.string())
                    .executes(context -> {
                        if(client.getConnection()==null||client.player == null||client.level==null)
                        {
                            return 0;
                        }
                        String sName = StringArgumentType.getString(context, "type");

                        if(currentControll!=null)
                        {
                            if(currentControll.isAlive())
                            {
                                Util.Kill(currentControll, client);
                            }
                            currentControll = null;
                        }

                        client.getConnection().sendCommand(String.format("summon %s ~ ~ ~ {CustomName:"+'"'+"%s"+'"'+",NoAI:1b,Invulnerable:1b}", sName, client.player.getName().getString()+"_"));



                        context.getSource().sendFeedback(Component.literal("Started!"));
                        return 1;
                    }))
            );
            dispatcher.register(ClientCommands.literal("stop_be")
                    .executes(context -> {
                        if(client.getConnection()==null||client.player == null||client.level==null)
                        {
                            return 0;
                        }
                        if(currentControll==null || !currentControll.isAlive())
                        {
                            context.getSource().sendError(Component.literal("You aren't controlling entities!"));
                            return 0;
                        }
                        Util.Kill(currentControll, client);
                        currentControll=null;
                        return 1;
                    })
            );
        });
    }
}
