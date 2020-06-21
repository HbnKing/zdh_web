package com.zyc.zdh.entity;

public class ZdhSqlInfo {

    //任务记录唯一标识(注意和调度任务的标识不一样)
    private String task_logs_id;

    private QuartzJobInfo quartzJobInfo;

    //输入数据源
    private Dsi_Info dsi_Input;


    //输出数据源
    private Dsi_Info dsi_Output;

    //etl 任务
    private SqlTaskInfo sqlTaskInfo;

    public String getTask_logs_id() {
        return task_logs_id;
    }

    public void setTask_logs_id(String task_logs_id) {
        this.task_logs_id = task_logs_id;
    }

    public QuartzJobInfo getQuartzJobInfo() {
        return quartzJobInfo;
    }

    public void setQuartzJobInfo(QuartzJobInfo quartzJobInfo) {
        this.quartzJobInfo = quartzJobInfo;
    }

    public Dsi_Info getDsi_Input() {
        return dsi_Input;
    }

    public void setDsi_Input(Dsi_Info dsi_Input) {
        this.dsi_Input = dsi_Input;
    }

    public Dsi_Info getDsi_Output() {
        return dsi_Output;
    }

    public void setDsi_Output(Dsi_Info dsi_Output) {
        this.dsi_Output = dsi_Output;
    }

    public SqlTaskInfo getSqlTaskInfo() {
        return sqlTaskInfo;
    }

    public void setSqlTaskInfo(SqlTaskInfo sqlTaskInfo) {
        this.sqlTaskInfo = sqlTaskInfo;
    }

    public void setZdhInfo(DataSourcesInfo dataSourcesInfoInput , SqlTaskInfo sqlTaskInfo, DataSourcesInfo dataSourcesInfoOutput,QuartzJobInfo quartzJobInfo){

        // this.dataSourcesInfoInput=dataSourcesInfoInput;

        // this.dataSourcesInfoOutput=dataSourcesInfoOutput;
        this.quartzJobInfo=quartzJobInfo;

        this.sqlTaskInfo=sqlTaskInfo;
        Dsi_Info dsi_Input=new Dsi_Info();
        dsi_Input.setId(dataSourcesInfoInput.getId());
        dsi_Input.setData_source_context(dataSourcesInfoInput.getData_source_context());
        dsi_Input.setData_source_type(dataSourcesInfoInput.getData_source_type());
        dsi_Input.setDbtable(null);
        dsi_Input.setDriver(dataSourcesInfoInput.getDriver());
        dsi_Input.setUrl(dataSourcesInfoInput.getUrl());
        dsi_Input.setUser(dataSourcesInfoInput.getUsername());
        dsi_Input.setPassword(dataSourcesInfoInput.getPassword());
        dsi_Input.setPaths(null);
        this.dsi_Input=dsi_Input;

        Dsi_Info dsi_Output=new Dsi_Info();
        dsi_Output.setId(dataSourcesInfoOutput.getId());
        dsi_Output.setData_source_context(dataSourcesInfoOutput.getData_source_context());
        dsi_Output.setData_source_type(dataSourcesInfoOutput.getData_source_type());
        dsi_Output.setDbtable(sqlTaskInfo.getData_sources_table_name_output());
        dsi_Output.setDriver(dataSourcesInfoOutput.getDriver());
        dsi_Output.setUrl(dataSourcesInfoOutput.getUrl());
        dsi_Output.setUser(dataSourcesInfoOutput.getUsername());
        dsi_Output.setPassword(dataSourcesInfoOutput.getPassword());
        dsi_Output.setPaths(sqlTaskInfo.getData_sources_file_name_output());

        this.dsi_Output=dsi_Output;

    }


}