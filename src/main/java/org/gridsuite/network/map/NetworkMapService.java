/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.network.map;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import com.powsybl.sld.iidm.extensions.BranchStatus;
import org.gridsuite.network.map.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@ComponentScan(basePackageClasses = {NetworkStoreService.class})
@Service
class NetworkMapService {

    @Autowired
    private NetworkStoreService networkStoreService;

    private Network getNetwork(UUID networkUuid, PreloadingStrategy strategy, String variantId) {
        try {
            Network network = networkStoreService.getNetwork(networkUuid, strategy);
            if (variantId != null) {
                network.getVariantManager().setWorkingVariant(variantId);
            }
            return network;
        } catch (PowsyblException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private static VoltageLevelMapData toMapData(VoltageLevel voltageLevel) {
        return VoltageLevelMapData.builder()
            .name(voltageLevel.getNameOrId())
            .id(voltageLevel.getId())
            .nominalVoltage(voltageLevel.getNominalV())
            .topologyKind(voltageLevel.getTopologyKind())
            .build();
    }

    private static SubstationMapData toMapData(Substation substation) {
        return SubstationMapData.builder()
            .name(substation.getNameOrId())
            .id(substation.getId())
            .countryName(substation.getCountry().map(Country::getName).orElse(null))
            .voltageLevels(substation.getVoltageLevelStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
            .build();
    }

    private static LineMapData toMapData(Line line) {
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();
        LineMapData.LineMapDataBuilder builder = LineMapData.builder()
            .name(line.getNameOrId())
            .id(line.getId())
            .terminal1Connected(terminal1.isConnected())
            .terminal2Connected(terminal2.isConnected())
            .voltageLevelId1(terminal1.getVoltageLevel().getId())
            .voltageLevelId2(terminal2.getVoltageLevel().getId());
        if (!Double.isNaN(terminal1.getP())) {
            builder.p1(terminal1.getP());
        }
        if (!Double.isNaN(terminal1.getQ())) {
            builder.q1(terminal1.getQ());
        }
        if (!Double.isNaN(terminal2.getP())) {
            builder.p2(terminal2.getP());
        }
        if (!Double.isNaN(terminal2.getQ())) {
            builder.q2(terminal2.getQ());
        }
        if (!Double.isNaN(terminal1.getI())) {
            builder.i1(terminal1.getI());
        }
        if (!Double.isNaN(terminal2.getI())) {
            builder.i2(terminal2.getI());
        }
        CurrentLimits limits1 = line.getCurrentLimits1();
        CurrentLimits limits2 = line.getCurrentLimits2();

        if (limits1 != null && !Double.isNaN(limits1.getPermanentLimit())) {
            builder.permanentLimit1(limits1.getPermanentLimit());
        }
        if (limits2 != null && !Double.isNaN(limits2.getPermanentLimit())) {
            builder.permanentLimit2(limits2.getPermanentLimit());
        }
        BranchStatus branchStatus = line.getExtension(BranchStatus.class);
        if (branchStatus != null) {
            builder.branchStatus(branchStatus.getStatus().name());
        }
        return builder.build();
    }

    private static GeneratorMapData toMapData(Generator generator) {
        Terminal terminal = generator.getTerminal();
        GeneratorMapData.GeneratorMapDataBuilder builder = GeneratorMapData.builder()
            .name(generator.getNameOrId())
            .id(generator.getId())
            .terminalConnected(terminal.isConnected())
            .voltageLevelId(terminal.getVoltageLevel().getId())
            .targetP(generator.getTargetP())
            .minP(generator.getMinP())
            .maxP(generator.getMaxP());
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        return builder.build();
    }

    private static TwoWindingsTransformerMapData toMapData(TwoWindingsTransformer transformer) {
        Terminal terminal1 = transformer.getTerminal1();
        Terminal terminal2 = transformer.getTerminal2();
        TwoWindingsTransformerMapData.TwoWindingsTransformerMapDataBuilder builder = TwoWindingsTransformerMapData.builder()
            .name(transformer.getNameOrId())
            .id(transformer.getId())
            .terminal1Connected(terminal1.isConnected())
            .terminal2Connected(terminal2.isConnected())
            .voltageLevelId1(terminal1.getVoltageLevel().getId())
            .voltageLevelId2(terminal2.getVoltageLevel().getId())
            .phaseTapChanger(toMapData(transformer.getPhaseTapChanger()))
            .ratioTapChanger(toMapData(transformer.getRatioTapChanger()));
        if (!Double.isNaN(terminal1.getP())) {
            builder.p1(terminal1.getP());
        }
        if (!Double.isNaN(terminal1.getQ())) {
            builder.q1(terminal1.getQ());
        }
        if (!Double.isNaN(terminal2.getP())) {
            builder.p2(terminal2.getP());
        }
        if (!Double.isNaN(terminal2.getQ())) {
            builder.q2(terminal2.getQ());
        }
        if (!Double.isNaN(terminal1.getI())) {
            builder.i1(terminal1.getI());
        }
        if (!Double.isNaN(terminal2.getI())) {
            builder.i2(terminal2.getI());
        }
        CurrentLimits limits1 = transformer.getCurrentLimits1();
        CurrentLimits limits2 = transformer.getCurrentLimits2();

        if (limits1 != null && !Double.isNaN(limits1.getPermanentLimit())) {
            builder.permanentLimit1(limits1.getPermanentLimit());
        }
        if (limits2 != null && !Double.isNaN(limits2.getPermanentLimit())) {
            builder.permanentLimit2(limits2.getPermanentLimit());
        }

        if (transformer.hasRatioTapChanger()) {
            builder.ratioTapChangerPosition(transformer.getRatioTapChanger().getTapPosition());
        }
        if (transformer.hasPhaseTapChanger()) {
            builder.phaseTapChangerPosition(transformer.getPhaseTapChanger().getTapPosition());
        }
        return builder.build();
    }

    private static TapChangerData toMapData(TapChanger<?, ?> tapChanger) {
        return tapChanger == null ? null : TapChangerData.builder()
            .lowTap(tapChanger.getLowTapPosition())
            .highTap(tapChanger.getHighTapPosition())
            .build();
    }

    private static ThreeWindingsTransformerMapData toMapData(ThreeWindingsTransformer transformer) {
        ThreeWindingsTransformer.Leg leg1 = transformer.getLeg1();
        ThreeWindingsTransformer.Leg leg2 = transformer.getLeg2();
        ThreeWindingsTransformer.Leg leg3 = transformer.getLeg3();

        Terminal terminal1 = leg1.getTerminal();
        Terminal terminal2 = leg2.getTerminal();
        Terminal terminal3 = leg3.getTerminal();

        ThreeWindingsTransformerMapData.ThreeWindingsTransformerMapDataBuilder builder = ThreeWindingsTransformerMapData.builder()
            .name(transformer.getNameOrId())
            .id(transformer.getId())
            .terminal1Connected(terminal1.isConnected())
            .terminal2Connected(terminal2.isConnected())
            .terminal3Connected(terminal3.isConnected())
            .voltageLevelId1(terminal1.getVoltageLevel().getId())
            .voltageLevelId2(terminal2.getVoltageLevel().getId())
            .voltageLevelId3(terminal3.getVoltageLevel().getId());
        if (!Double.isNaN(terminal1.getP())) {
            builder.p1(terminal1.getP());
        }
        if (!Double.isNaN(terminal1.getQ())) {
            builder.q1(terminal1.getQ());
        }
        if (!Double.isNaN(terminal2.getP())) {
            builder.p2(terminal2.getP());
        }
        if (!Double.isNaN(terminal2.getQ())) {
            builder.q2(terminal2.getQ());
        }
        if (!Double.isNaN(terminal3.getP())) {
            builder.p3(terminal3.getP());
        }
        if (!Double.isNaN(terminal3.getQ())) {
            builder.q3(terminal3.getQ());
        }
        if (!Double.isNaN(terminal1.getI())) {
            builder.i1(terminal1.getI());
        }
        if (!Double.isNaN(terminal2.getI())) {
            builder.i2(terminal2.getI());
        }
        if (!Double.isNaN(terminal3.getI())) {
            builder.i3(terminal3.getI());
        }

        CurrentLimits limits1 = leg1.getCurrentLimits();
        CurrentLimits limits2 = leg2.getCurrentLimits();
        CurrentLimits limits3 = leg3.getCurrentLimits();

        if (limits1 != null && !Double.isNaN(limits1.getPermanentLimit())) {
            builder.permanentLimit1(limits1.getPermanentLimit());
        }
        if (limits2 != null && !Double.isNaN(limits2.getPermanentLimit())) {
            builder.permanentLimit2(limits2.getPermanentLimit());
        }
        if (limits3 != null && !Double.isNaN(limits3.getPermanentLimit())) {
            builder.permanentLimit3(limits3.getPermanentLimit());
        }

        if (leg1.hasRatioTapChanger()) {
            builder.ratioTapChanger1Position(leg1.getRatioTapChanger().getTapPosition());
            builder.ratioTapChanger1(toMapData(leg1.getRatioTapChanger()));
        }
        if (leg2.hasRatioTapChanger()) {
            builder.ratioTapChanger2Position(leg2.getRatioTapChanger().getTapPosition());
            builder.ratioTapChanger2(toMapData(leg2.getRatioTapChanger()));
        }
        if (leg3.hasRatioTapChanger()) {
            builder.ratioTapChanger3Position(leg3.getRatioTapChanger().getTapPosition());
            builder.ratioTapChanger3(toMapData(leg3.getRatioTapChanger()));
        }

        if (leg1.hasPhaseTapChanger()) {
            builder.phaseTapChanger1Position(leg1.getPhaseTapChanger().getTapPosition());
            builder.phaseTapChanger1(toMapData(leg1.getPhaseTapChanger()));
        }
        if (leg2.hasPhaseTapChanger()) {
            builder.phaseTapChanger2Position(leg2.getPhaseTapChanger().getTapPosition());
            builder.phaseTapChanger2(toMapData(leg2.getPhaseTapChanger()));
        }
        if (leg3.hasPhaseTapChanger()) {
            builder.phaseTapChanger3Position(leg3.getPhaseTapChanger().getTapPosition());
            builder.phaseTapChanger3(toMapData(leg3.getPhaseTapChanger()));
        }

        return builder.build();
    }

    private static BatteryMapData toMapData(Battery battery) {
        Terminal terminal = battery.getTerminal();
        BatteryMapData.BatteryMapDataBuilder builder = BatteryMapData.builder()
            .name(battery.getNameOrId())
            .id(battery.getId())
            .terminalConnected(terminal.isConnected())
            .voltageLevelId(terminal.getVoltageLevel().getId())
            .p0(battery.getP0())
            .q0(battery.getQ0());
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        return builder.build();
    }

    private static DanglingLineMapData toMapData(DanglingLine danglingLine) {
        Terminal terminal = danglingLine.getTerminal();
        DanglingLineMapData.DanglingLineMapDataBuilder builder = DanglingLineMapData.builder()
            .name(danglingLine.getNameOrId())
            .id(danglingLine.getId())
            .terminalConnected(terminal.isConnected())
            .voltageLevelId(terminal.getVoltageLevel().getId())
            .ucteXnodeCode(danglingLine.getUcteXnodeCode())
            .p0(danglingLine.getP0())
            .q0(danglingLine.getQ0());
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        return builder.build();
    }

    private static HvdcLineMapData toMapData(HvdcLine hvdcLine) {
        HvdcLineMapData.HvdcLineMapDataBuilder builder = HvdcLineMapData.builder()
            .name(hvdcLine.getNameOrId())
            .id(hvdcLine.getId())
            .convertersMode(hvdcLine.getConvertersMode())
            .converterStationId1(hvdcLine.getConverterStation1().getId())
            .converterStationId2(hvdcLine.getConverterStation2().getId())
            .activePowerSetpoint(hvdcLine.getActivePowerSetpoint());
        return builder.build();
    }

    private static LccConverterStationMapData toMapData(LccConverterStation lccConverterStation) {
        Terminal terminal = lccConverterStation.getTerminal();
        LccConverterStationMapData.LccConverterStationMapDataBuilder builder = LccConverterStationMapData.builder()
            .name(lccConverterStation.getNameOrId())
            .id(lccConverterStation.getId())
            .voltageLevelId(terminal.getVoltageLevel().getId())
            .terminalConnected(terminal.isConnected())
            .lossFactor(lccConverterStation.getLossFactor())
            .powerFactor(lccConverterStation.getPowerFactor());
        if (lccConverterStation.getHvdcLine() != null) {
            builder.hvdcLineId(lccConverterStation.getHvdcLine().getId());
        }
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        return builder.build();
    }

    private static VscConverterStationMapData toMapData(VscConverterStation vscConverterStation) {
        Terminal terminal = vscConverterStation.getTerminal();
        VscConverterStationMapData.VscConverterStationMapDataBuilder builder = VscConverterStationMapData.builder()
            .name(vscConverterStation.getNameOrId())
            .id(vscConverterStation.getId())
            .voltageLevelId(terminal.getVoltageLevel().getId())
            .terminalConnected(terminal.isConnected())
            .lossFactor(vscConverterStation.getLossFactor());
        if (vscConverterStation.getHvdcLine() != null) {
            builder.hvdcLineId(vscConverterStation.getHvdcLine().getId());
        }
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        return builder.build();
    }

    private static LoadMapData toMapData(Load load) {
        Terminal terminal = load.getTerminal();
        LoadMapData.LoadMapDataBuilder builder = LoadMapData.builder()
            .name(load.getNameOrId())
            .id(load.getId())
            .type(load.getLoadType())
            .terminalConnected(terminal.isConnected())
            .voltageLevelId(terminal.getVoltageLevel().getId())
            .p0(load.getP0())
            .q0(load.getQ0());
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        return builder.build();
    }

    private static ShuntCompensatorMapData toMapData(ShuntCompensator shuntCompensator) {
        Terminal terminal = shuntCompensator.getTerminal();
        ShuntCompensatorMapData.ShuntCompensatorMapDataBuilder builder = ShuntCompensatorMapData.builder()
            .name(shuntCompensator.getNameOrId())
            .id(shuntCompensator.getId())
            .terminalConnected(terminal.isConnected())
            .voltageLevelId(terminal.getVoltageLevel().getId());
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        if (!Double.isNaN(shuntCompensator.getTargetV())) {
            builder.targetV(shuntCompensator.getTargetV());
        }
        if (!Double.isNaN(shuntCompensator.getTargetDeadband())) {
            builder.targetDeadband(shuntCompensator.getTargetDeadband());
        }
        return builder.build();
    }

    private static StaticVarCompensatorMapData toMapData(StaticVarCompensator staticVarCompensator) {
        Terminal terminal = staticVarCompensator.getTerminal();
        StaticVarCompensatorMapData.StaticVarCompensatorMapDataBuilder builder = StaticVarCompensatorMapData.builder()
            .name(staticVarCompensator.getNameOrId())
            .id(staticVarCompensator.getId())
            .terminalConnected(terminal.isConnected())
            .voltageLevelId(terminal.getVoltageLevel().getId())
            .regulationMode(staticVarCompensator.getRegulationMode());
        if (!Double.isNaN(terminal.getP())) {
            builder.p(terminal.getP());
        }
        if (!Double.isNaN(terminal.getQ())) {
            builder.q(terminal.getQ());
        }
        if (!Double.isNaN(staticVarCompensator.getVoltageSetpoint())) {
            builder.voltageSetpoint(staticVarCompensator.getVoltageSetpoint());
        }
        if (!Double.isNaN(staticVarCompensator.getReactivePowerSetpoint())) {
            builder.reactivePowerSetpoint(staticVarCompensator.getReactivePowerSetpoint());
        }
        return builder.build();
    }

    private static BusMapData toMapData(Bus bus) {
        return BusMapData.builder()
            .name(bus.getNameOrId())
            .id(bus.getId())
            .build();
    }

    private static BusbarSectionMapData toMapData(BusbarSection busbarSection) {
        return BusbarSectionMapData.builder()
            .name(busbarSection.getNameOrId())
            .id(busbarSection.getId())
            .build();
    }

    public List<SubstationMapData> getSubstations(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getSubstationStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            List<SubstationMapData> res = new ArrayList<>();
            substationsId.stream().forEach(id -> res.add(toMapData(network.getSubstation(id))));
            return res;
        }
    }

    public List<LineMapData> getLines(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getLineStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<LineMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(Line.class).forEach(l -> res.add(toMapData(l)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<GeneratorMapData> getGenerators(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getGeneratorStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<GeneratorMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(Generator.class).forEach(g -> res.add(toMapData(g)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<TwoWindingsTransformerMapData> getTwoWindingsTransformers(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getTwoWindingsTransformerStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<TwoWindingsTransformerMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(TwoWindingsTransformer.class).forEach(t -> res.add(toMapData(t)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<ThreeWindingsTransformerMapData> getThreeWindingsTransformers(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getThreeWindingsTransformerStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<ThreeWindingsTransformerMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(ThreeWindingsTransformer.class).forEach(t -> res.add(toMapData(t)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public AllMapData getAll(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);

        if (substationsId == null) {
            return AllMapData.builder()
                .substations(network.getSubstationStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .lines(network.getLineStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .generators(network.getGeneratorStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .twoWindingsTransformers(network.getTwoWindingsTransformerStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .threeWindingsTransformers(network.getThreeWindingsTransformerStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .batteries(network.getBatteryStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .danglingLines(network.getDanglingLineStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .hvdcLines(network.getHvdcLineStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .lccConverterStations(network.getLccConverterStationStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .loads(network.getLoadStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .shuntCompensators(network.getShuntCompensatorStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .staticVarCompensators(network.getStaticVarCompensatorStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .vscConverterStations(network.getVscConverterStationStream().map(NetworkMapService::toMapData).collect(Collectors.toList()))
                .build();
        } else {
            Set<SubstationMapData> substationsMap = new LinkedHashSet<>();
            Set<LineMapData> linesMap = new LinkedHashSet<>();
            Set<GeneratorMapData> generatorsMap = new LinkedHashSet<>();
            Set<TwoWindingsTransformerMapData> twoWindingsTransformersMap = new LinkedHashSet<>();
            Set<ThreeWindingsTransformerMapData> threeWindingsTransformersMap = new LinkedHashSet<>();
            Set<BatteryMapData> batteriesMap = new LinkedHashSet<>();
            Set<DanglingLineMapData> danglingLinesMap = new LinkedHashSet<>();
            Set<HvdcLineMapData> hvdcLinesMap = new LinkedHashSet<>();
            Set<LccConverterStationMapData> lccConverterStationsMap = new LinkedHashSet<>();
            Set<LoadMapData> loadsMap = new LinkedHashSet<>();
            Set<ShuntCompensatorMapData> shuntCompensatorsMap = new LinkedHashSet<>();
            Set<StaticVarCompensatorMapData> staticVarCompensatorsMap = new LinkedHashSet<>();
            Set<VscConverterStationMapData> vscConverterStationsMap = new LinkedHashSet<>();

            substationsId.stream().forEach(id -> {
                Substation substation = network.getSubstation(id);
                substationsMap.add(toMapData(substation));
                substation.getVoltageLevelStream().forEach(v ->
                    v.getConnectables().forEach(c -> {
                        switch (c.getType()) {
                            case LINE:
                                linesMap.add(toMapData((Line) c));
                                break;
                            case TWO_WINDINGS_TRANSFORMER:
                                twoWindingsTransformersMap.add(toMapData((TwoWindingsTransformer) c));
                                break;
                            case THREE_WINDINGS_TRANSFORMER:
                                threeWindingsTransformersMap.add(toMapData((ThreeWindingsTransformer) c));
                                break;
                            case GENERATOR:
                                generatorsMap.add(toMapData((Generator) c));
                                break;
                            case BATTERY:
                                batteriesMap.add(toMapData((Battery) c));
                                break;
                            case LOAD:
                                loadsMap.add(toMapData((Load) c));
                                break;
                            case SHUNT_COMPENSATOR:
                                shuntCompensatorsMap.add(toMapData((ShuntCompensator) c));
                                break;
                            case DANGLING_LINE:
                                danglingLinesMap.add(toMapData((DanglingLine) c));
                                break;
                            case STATIC_VAR_COMPENSATOR:
                                staticVarCompensatorsMap.add(toMapData((StaticVarCompensator) c));
                                break;
                            case HVDC_CONVERTER_STATION: {
                                HvdcConverterStation<?> hdvcConverter = (HvdcConverterStation<?>) c;
                                HvdcLine hvdcLine = hdvcConverter.getHvdcLine();
                                if (hvdcLine != null) {
                                    hvdcLinesMap.add(toMapData(hvdcLine));
                                }
                                if (hdvcConverter.getHvdcType() == HvdcConverterStation.HvdcType.LCC) {
                                    lccConverterStationsMap.add(toMapData((LccConverterStation) hdvcConverter));
                                } else {
                                    vscConverterStationsMap.add(toMapData((VscConverterStation) hdvcConverter));
                                }
                            }
                            break;
                            default:
                        }
                    })
                );
            });
            return AllMapData.builder()
                .substations(substationsMap.stream().collect(Collectors.toList()))
                .lines(linesMap.stream().collect(Collectors.toList()))
                .generators(generatorsMap.stream().collect(Collectors.toList()))
                .twoWindingsTransformers(twoWindingsTransformersMap.stream().collect(Collectors.toList()))
                .threeWindingsTransformers(threeWindingsTransformersMap.stream().collect(Collectors.toList()))
                .batteries(batteriesMap.stream().collect(Collectors.toList()))
                .danglingLines(danglingLinesMap.stream().collect(Collectors.toList()))
                .hvdcLines(hvdcLinesMap.stream().collect(Collectors.toList()))
                .lccConverterStations(lccConverterStationsMap.stream().collect(Collectors.toList()))
                .loads(loadsMap.stream().collect(Collectors.toList()))
                .shuntCompensators(shuntCompensatorsMap.stream().collect(Collectors.toList()))
                .staticVarCompensators(staticVarCompensatorsMap.stream().collect(Collectors.toList()))
                .vscConverterStations(vscConverterStationsMap.stream().collect(Collectors.toList()))
                .build();
        }
    }

    public List<BatteryMapData> getBatteries(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getBatteryStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<BatteryMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(Battery.class).forEach(b -> res.add(toMapData(b)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<DanglingLineMapData> getDanglingLines(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getDanglingLineStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<DanglingLineMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(DanglingLine.class).forEach(d -> res.add(toMapData(d)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<HvdcLineMapData> getHvdcLines(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getHvdcLineStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<HvdcLineMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(HvdcConverterStation.class).forEach(h -> {
                        HvdcLine hvdcLine = h.getHvdcLine();
                        if (hvdcLine != null) {
                            res.add(toMapData(hvdcLine));
                        }
                    })));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<LccConverterStationMapData> getLccConverterStations(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getLccConverterStationStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<LccConverterStationMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(LccConverterStation.class).forEach(l -> res.add(toMapData(l)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<LoadMapData> getLoads(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getLoadStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<LoadMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(Load.class).forEach(l -> res.add(toMapData(l)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<ShuntCompensatorMapData> getShuntCompensators(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getShuntCompensatorStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<ShuntCompensatorMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(ShuntCompensator.class).forEach(s -> res.add(toMapData(s)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<StaticVarCompensatorMapData> getStaticVarCompensators(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getStaticVarCompensatorStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<StaticVarCompensatorMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(StaticVarCompensator.class).forEach(s -> res.add(toMapData(s)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<VscConverterStationMapData> getVscConverterStations(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getVscConverterStationStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<VscConverterStationMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v ->
                    v.getConnectables(VscConverterStation.class).forEach(s -> res.add(toMapData(s)))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<VoltageLevelMapData> getVoltageLevels(UUID networkUuid, String variantId, List<String> substationsId) {
        Network network = getNetwork(networkUuid, substationsId == null ? PreloadingStrategy.COLLECTION : PreloadingStrategy.NONE, variantId);
        if (substationsId == null) {
            return network.getVoltageLevelStream()
                .map(NetworkMapService::toMapData).collect(Collectors.toList());
        } else {
            Set<VoltageLevelMapData> res = new LinkedHashSet<>();
            substationsId.stream().forEach(id ->
                network.getSubstation(id).getVoltageLevelStream().forEach(v -> res.add(toMapData(v))));
            return res.stream().collect(Collectors.toList());
        }
    }

    public List<BusMapData> getVoltageLevelBuses(UUID networkUuid, String voltageLevelId, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        return network.getVoltageLevel(voltageLevelId).getBusBreakerView().getBusStream()
            .map(NetworkMapService::toMapData).collect(Collectors.toList());
    }

    public List<BusbarSectionMapData> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId, String variantId) {
        Network network = getNetwork(networkUuid, PreloadingStrategy.NONE, variantId);
        return network.getVoltageLevel(voltageLevelId).getNodeBreakerView().getBusbarSectionStream()
            .map(NetworkMapService::toMapData).collect(Collectors.toList());
    }
}
