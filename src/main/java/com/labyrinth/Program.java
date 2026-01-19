package com.labyrinth;

import com.hypixel.hytale.server.core.event.events.entity.LivingEntityInventoryChangeEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.labyrinth.core.database.HibernateMiddleware;
import com.labyrinth.portal_scrolls.PortalScrollEntity;
import com.labyrinth.portal_scrolls.ScrollEvents;
import com.labyrinth.portal_scrolls.ScrollPlaceBlockSystem;

import javax.annotation.Nonnull;

public class Program extends JavaPlugin {

    private static Program INSTANCE;

    public Program(@Nonnull JavaPluginInit init) {
        super(init);
        INSTANCE = this;
    }

    public Program getInstance() {
        return INSTANCE;
    }

    @Override
    protected void setup() {
        registerCommands();
        registerEvents();
        registerSystems();
    }

    @Override
    protected void start() {
        HibernateMiddleware.getInstance().connect(PortalScrollEntity.class);
    }

    @Override
    protected void shutdown() {
        HibernateMiddleware.getInstance().disconnect();
    }

    private void registerCommands() {
    }

    private void registerEvents() {
        getEventRegistry().registerGlobal(LivingEntityInventoryChangeEvent.class, ScrollEvents::on);
    }

    private void registerSystems() {
        getEntityStoreRegistry().registerSystem(new ScrollPlaceBlockSystem());
    }
}
