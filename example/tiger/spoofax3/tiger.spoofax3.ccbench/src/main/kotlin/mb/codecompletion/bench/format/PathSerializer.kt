package mb.codecompletion.bench.format

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Custom path serializer.
 */
class PathSerializer: StdScalarSerializer<Path>(Path::class.java) {

    @Throws(IOException::class)
    override fun serialize(value: Path, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.toString())
    }

    @Throws(IOException::class)
    override fun serializeWithType(
        value: Path,
        g: JsonGenerator,
        provider: SerializerProvider,
        typeSer: TypeSerializer
    ) {
        val typeIdDef = typeSer.writeTypePrefix(g, typeSer.typeId(value, Path::class.java, JsonToken.VALUE_STRING))
        serialize(value, g, provider)
        typeSer.writeTypeSuffix(g, typeIdDef)
    }

}

/**
 * Custom path deserializer.
 */
class PathDeserializer: StdScalarDeserializer<Path>(Path::class.java) {

    @Throws(IOException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Path {
        if (!p.hasToken(JsonToken.VALUE_STRING)) return ctxt.handleUnexpectedToken(Path::class.java, p) as Path
        val value = p.text
        return Paths.get(value)
    }
}
