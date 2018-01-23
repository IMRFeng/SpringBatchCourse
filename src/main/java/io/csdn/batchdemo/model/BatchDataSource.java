package io.csdn.batchdemo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

/**
 * @author Zhantao Feng.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "DATA_SOURCE")
@DynamicUpdate
public class BatchDataSource {

    @Id
    @GeneratedValue
    private int id;

    @Column(name = "SUB_CLASS")
    private String subClass;

    @Column(name = "PARENT_CLASS")
    private String parentClass;

    @Column(name = "DATA_TEXT")
    private String dataText;

    @Column(name = "COLUMN_NUMBER")
    private int columnNumber;
}
