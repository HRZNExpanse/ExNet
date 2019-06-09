/*
 * Copyright (C) 2019 Horizon Studio (https://hrznstudio.com/)
 *
 * This file is part of ExNet.
 *
 * ExNet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ExNet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ExNet.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.hrznstudio.exnet

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.artemis.io.JsonArtemisSerializer
import com.artemis.managers.WorldSerializationManager

/**
 * Instructs the Kotlin compiler to generate a no-arg constructor.
 * This is required on Component data classes.
 */
annotation class Noarg

val serializationManager = WorldSerializationManager()

fun world(configuration: WorldConfigurationBuilder.() -> WorldConfigurationBuilder): World = World(
    WorldConfigurationBuilder()
        .with(WorldConfigurationBuilder.Priority.LOWEST, NetworkingSenderSystem())
        .with(serializationManager)
        .configuration()
        .build()
).apply {
    // Using json while debugging
//    serializationManager.setSerializer(KryoArtemisSerializer(this))
    serializationManager.setSerializer(JsonArtemisSerializer(this))
}
