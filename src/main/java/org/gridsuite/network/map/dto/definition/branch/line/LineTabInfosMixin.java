package org.gridsuite.network.map.dto.definition.branch.line;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.gridsuite.network.map.dto.definition.branch.BranchTabInfos;
import org.springframework.boot.jackson.JsonMixin;

/**
 * Jackson mixin of {@link LineTabInfos} to add annotations on some fields from {@link BranchTabInfos}.
 */
@SuppressWarnings("checkstyle:abstractclassname")
@JsonMixin(LineTabInfos.class)
abstract class LineTabInfosMixin {
    @JsonInclude(Include.NON_NULL)
    public abstract String getVoltageLevelName1();

    @JsonInclude(Include.NON_NULL)
    public abstract String getVoltageLevelName2();

    @JsonInclude(Include.NON_NULL)
    public abstract Double getR();

    @JsonInclude(Include.NON_NULL)
    public abstract Double getX();
}
