package com.erpnext.pos.remoteSource.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

//TODO: Mover
object IntAsBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BooleanAsInt", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeInt(if (value) 1 else 0)
    }

    override fun deserialize(decoder: Decoder): Boolean {
        return decoder.decodeInt() != 0
    }
}

data class ItemDtoResponse(
    val data: List<ItemDto>
)

@Serializable
data class ItemDto(
    @SerialName("item_name")
    var name: String = "",
    @SerialName("item_code")
    var itemCode: String = "",
    @SerialName("description")
    var description: String,
    var barcode: String = "",
    @SerialName("item_group")
    var itemGroup: String = "",
    var brand: String? = null,
    var image: String? = null,
    var price: Double = 0.0,
    @SerialName("cost_price")
    var actualQty: Double = 0.0,
    var discount: Double = 0.0,
    var isService: Boolean = false,
    var isStocked: Boolean = false,
    @SerialName("stock_uom")
    var stockUom: String,
    @Serializable(with = IntAsBooleanSerializer::class)
    @SerialName("disabled")
    var isDisabled: Boolean = false,
)