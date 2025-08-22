package org.gridsuite.network.map.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import org.gridsuite.network.map.dto.ElementInfos.InfoType;
import org.gridsuite.network.map.dto.ElementType;
import org.gridsuite.network.map.dto.definition.battery.BatteryTabInfos;
import org.gridsuite.network.map.dto.definition.bus.BusTabInfos;
import org.gridsuite.network.map.dto.definition.busbarsection.BusBarSectionTabInfos;
import org.gridsuite.network.map.dto.definition.danglingline.DanglingLineTabInfos;
import org.gridsuite.network.map.dto.definition.generator.GeneratorTabInfos;
import org.gridsuite.network.map.dto.definition.hvdc.HvdcTabInfos;
import org.gridsuite.network.map.dto.definition.lccconverterstation.LccConverterStationTabInfos;
import org.gridsuite.network.map.dto.definition.line.LineTabInfos;
import org.gridsuite.network.map.dto.definition.load.LoadTabInfos;
import org.gridsuite.network.map.dto.definition.shuntcompensator.ShuntCompensatorTabInfos;
import org.gridsuite.network.map.dto.definition.staticvarcompensator.StaticVarCompensatorTabInfos;
import org.gridsuite.network.map.dto.definition.substation.SubstationTabInfos;
import org.gridsuite.network.map.dto.definition.threewindingstransformer.ThreeWindingsTransformerTabInfos;
import org.gridsuite.network.map.dto.definition.tieline.TieLineTabInfos;
import org.gridsuite.network.map.dto.definition.twowindingstransformer.TwoWindingsTransformerTabInfos;
import org.gridsuite.network.map.dto.definition.voltagelevel.VoltageLevelTabInfos;
import org.gridsuite.network.map.dto.definition.vscconverterstation.VscConverterStationTabInfos;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;

@Service
public class SchemaService {
    //TODO transform to a EnumMap<ElementType, EnumMap<InfoType, String>> later
    private final EnumMap<ElementType, String> tabInfosSchemas;

    /**
     * @apiNote use class instance to be more secure with enum and classes rename/moving/etc with IDE
     */
    private static Class<?> getTabInfosClass(final ElementType elementType) {
        return switch (elementType) {
            case BATTERY -> BatteryTabInfos.class;
            case BUS -> BusTabInfos.class;
            case BUSBAR_SECTION -> BusBarSectionTabInfos.class;
            case DANGLING_LINE -> DanglingLineTabInfos.class;
            case GENERATOR -> GeneratorTabInfos.class;
            case HVDC_LINE, HVDC_LINE_LCC, HVDC_LINE_VSC -> HvdcTabInfos.class;
            case LCC_CONVERTER_STATION -> LccConverterStationTabInfos.class;
            case LINE -> LineTabInfos.class;
            case LOAD -> LoadTabInfos.class;
            case SHUNT_COMPENSATOR -> ShuntCompensatorTabInfos.class;
            case STATIC_VAR_COMPENSATOR -> StaticVarCompensatorTabInfos.class;
            case SUBSTATION -> SubstationTabInfos.class;
            case THREE_WINDINGS_TRANSFORMER -> ThreeWindingsTransformerTabInfos.class;
            case TIE_LINE -> TieLineTabInfos.class;
            case TWO_WINDINGS_TRANSFORMER -> TwoWindingsTransformerTabInfos.class;
            case VOLTAGE_LEVEL -> VoltageLevelTabInfos.class;
            case VSC_CONVERTER_STATION -> VscConverterStationTabInfos.class;
        };
    }

    /**
     * Minimify the JSON and store in RAM for performance in giving it to clients.
     * @implNote Not done as static because if a file is missing, the exception will block the class loading by the classloader.
     */
    public SchemaService(final ResourceLoader resourceLoader) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper(); //just need a simple parser to minimize
        this.tabInfosSchemas = new EnumMap<>(ElementType.class);
        final var cl = this.getClass().getClassLoader();
        for (ElementType elementType : ElementType.values()) {
            this.tabInfosSchemas.put(elementType,
                objectMapper.readTree(resourceLoader.getResource("classpath:schemas/" + getTabInfosClass(elementType).getCanonicalName().replace('.', '/') + "-schema.json").getContentAsString(StandardCharsets.UTF_8)
            ).toString()); // we store minimized version of the json
        }
    }

    public String getSchema(@NonNull final ElementType elementType, @NonNull final InfoType infoType) {
        if (infoType != InfoType.TAB) {
            throw new UnsupportedOperationException("This info type is not currently supported.");
        }
        return tabInfosSchemas.get(elementType);
    }
}
