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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.channels.mapNotNull
import kotlin.random.Random

private val WORKER_ID = Random.nextInt() // TODO: this is probably not the right place to set it

@UseExperimental(ObsoleteCoroutinesApi::class)
fun ReceiveChannel<ByteArray>.mapUpdates() : ReceiveChannel<Update<*>> = this.mapNotNull { JSON.readValue<Update<*>?>(it) }
fun ReceiveChannel<Update<*>>.mapBytes() : ReceiveChannel<ByteArray> = this.map { JSON.writeValueAsBytes(it) }

@JsonSerialize(using = UpdateSerializer::class)
@JsonDeserialize(using = UpdateDeserializer::class)
data class Update<T: Value>(
    val entityId: Int,
    val component: Component<T>,
    val value: T?,
    val workerId: Int = WORKER_ID
)

class UpdateSerializer: JsonSerializer<Update<*>>() {
    override fun serialize(value: Update<*>, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeNumberField("workerId", value.workerId)
        gen.writeNumberField("entityId", value.entityId)
        gen.writeNumberField("componentId", value.component.id)
        gen.writeObjectField("value", value.value)
        gen.writeEndObject()
    }
}

class UpdateDeserializer: JsonDeserializer<Update<*>?>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Update<*>? {
        val root = p.codec.readTree<JsonNode>(p)
        val workerId = root.get("workerId").asInt()
        if (workerId == WORKER_ID) return null
        @Suppress("UNCHECKED_CAST")
        val component = ComponentStore[root.get("componentId").asInt()] as? Component<Value>
            ?: return null
        val entityId = root.get("entityId").asInt()
        val value = JSON.treeToValue(root.get("value"), component.clazz)
        return Update(entityId, component, value, workerId)
    }
}
