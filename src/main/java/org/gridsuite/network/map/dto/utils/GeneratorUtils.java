package org.gridsuite.network.map.dto.generator;


import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import lombok.Getter;
import lombok.Setter;

public final class GeneratorUtils {
    protected static GeneratorUtils.GeneratorInfos getGeneratorInfos(Generator generator) {
        GeneratorUtils.GeneratorInfos generatorInfos = new GeneratorUtils.GeneratorInfos();

        ActivePowerControl<Generator> activePowerControl = generator.getExtension(ActivePowerControl.class);
        if (activePowerControl != null) {
            generatorInfos.setActivePowerControlOn(activePowerControl.isParticipate());
            generatorInfos.setDroop(activePowerControl.getDroop());
        }
        return generatorInfos;
    }

    @Getter
    @Setter
    protected static class GeneratorInfos {
        Boolean activePowerControlOn;
        Double droop;
    }


    // Add more utility methods as needed...

}

