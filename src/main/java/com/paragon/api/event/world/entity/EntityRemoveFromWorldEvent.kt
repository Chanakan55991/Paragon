package com.paragon.api.event.world.entity

import me.wolfsurge.cerauno.event.Event
import net.minecraft.entity.Entity

/**
 * @author Wolfsurge, SooStrator1136
 */
class EntityRemoveFromWorldEvent(val entity: Entity) : Event()