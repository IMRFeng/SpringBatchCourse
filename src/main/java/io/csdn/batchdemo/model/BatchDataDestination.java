package io.csdn.batchdemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author Zhantao Feng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "DATA_DESTINATION")
public class BatchDataDestination {

    @Id
    private int id;

    @Column(name = "SUB_CLASS")
    private String subClass;

    @Column(name = "PARENT_CLASS")
    private String parentClass;

    @Column(name = "DATA_TEXT1")
    private String dataText1;

    @Column(name = "DATA_TEXT2")
    private String dataText2;

    @Column(name = "DATA_TEXT3")
    private String dataText3;

    @Column(name = "DATA_TEXT4")
    private String dataText4;

    @Column(name = "DATA_TEXT5")
    private String dataText5;

    @Column(name = "DATA_TEXT6")
    private String dataText6;

    @Column(name = "DATA_TEXT7")
    private String dataText7;

    @Column(name = "DATA_TEXT8")
    private String dataText8;

    @Column(name = "DATA_TEXT9")
    private String dataText9;

    @Column(name = "DATA_TEXT10")
    private String dataText10;
}
