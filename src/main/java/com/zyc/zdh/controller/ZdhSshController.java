package com.zyc.zdh.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.SftpException;
import com.zyc.zdh.dao.*;
import com.zyc.zdh.entity.*;
import com.zyc.zdh.job.SnowflakeIdWorker;
import com.zyc.zdh.util.DateUtil;
import com.zyc.zdh.util.SFTPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class ZdhSshController extends BaseController{


    @Autowired
    ZdhNginxMapper zdhNginxMapper;
    @Autowired
    EtlTaskUpdateLogsMapper etlTaskUpdateLogsMapper;
    @Autowired
    JarTaskMapper jarTaskMapper;
    @Autowired
    JarFileMapper jarFileMapper;
    @Autowired
    SshTaskMapper sshTaskMapper;


    @RequestMapping("/etl_task_jar_add_file")
    @ResponseBody
    public String etl_task_jar_add_file(MultipartFile[] jar_files, JarTaskInfo jarTaskInfo, HttpServletRequest request) {
        String json_str = JSON.toJSONString(request.getParameterMap());
        String owner = getUser().getId();
        String id = SnowflakeIdWorker.getInstance().nextId() + "";
        jarTaskInfo.setId(id);
        jarTaskInfo.setOwner(owner);
        jarTaskInfo.setFiles("");
        jarTaskInfo.setCreate_time(new Timestamp(new Date().getTime()));
        debugInfo(jarTaskInfo);
        jarTaskMapper.insert(jarTaskInfo);
        System.out.println(json_str);
        System.out.println(jar_files);
        if (jar_files != null && jar_files.length > 0) {
            for (MultipartFile jar_file : jar_files) {
                String fileName = jar_file.getOriginalFilename();
                System.out.println("上传文件不为空");
                JarFileInfo jarFileInfo = new JarFileInfo();
                jarFileInfo.setId(SnowflakeIdWorker.getInstance().nextId() + "");
                jarFileInfo.setJar_etl_id(id);
                jarFileInfo.setFile_name(fileName);
                jarFileInfo.setCreate_time(DateUtil.formatTime(new Timestamp(new Date().getTime())));
                jarFileInfo.setOwner(owner);
                jarFileMapper.insert(jarFileInfo);

                ZdhNginx zdhNginx = zdhNginxMapper.selectByOwner(owner);
                File tempFile = new File(zdhNginx.getTmp_dir() + "/" + owner + "/" + fileName);
                File fileDir = new File(zdhNginx.getTmp_dir() + "/" + owner);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                String nginx_dir = zdhNginx.getNginx_dir();
                try {
                    FileCopyUtils.copy(jar_file.getInputStream(), Files.newOutputStream(tempFile.toPath()));
                    if (!zdhNginx.getHost().equals("")) {
                        System.out.println("通过sftp上传文件");
                        SFTPUtil sftp = new SFTPUtil(zdhNginx.getUsername(), zdhNginx.getPassword(),
                                zdhNginx.getHost(), new Integer(zdhNginx.getPort()));
                        sftp.login();
                        InputStream is = new FileInputStream(tempFile);
                        sftp.upload(nginx_dir + "/" + owner + "/", fileName, is);
                        sftp.logout();
                    }
                    jarFileInfo.setStatus("success");
                    jarFileMapper.updateByPrimaryKey(jarFileInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            }


        }


        JSONObject json = new JSONObject();

        json.put("success", "200");
        return json.toJSONString();
    }


    @RequestMapping("/etl_task_jar_update")
    @ResponseBody
    public String etl_task_jar_update(MultipartFile[] jar_files, JarTaskInfo jarTaskInfo, HttpServletRequest request){
        String json_str = JSON.toJSONString(request.getParameterMap());
        String owner = getUser().getId();
        String id =jarTaskInfo.getId();
        jarTaskInfo.setOwner(owner);
        debugInfo(jarTaskInfo);
        jarTaskMapper.updateByPrimaryKey(jarTaskInfo);
        System.out.println(json_str);

        if (jar_files != null && jar_files.length > 0) {
            for (MultipartFile jar_file : jar_files) {
                String fileName = jar_file.getOriginalFilename();
                if(fileName.isEmpty()){
                    System.out.println("上传文件名称为空"+fileName);
                    continue;
                }
                System.out.println("上传文件不为空"+fileName);
                JarFileInfo jarFileInfo = new JarFileInfo();
                jarFileInfo.setId(SnowflakeIdWorker.getInstance().nextId() + "");
                jarFileInfo.setJar_etl_id(id);
                jarFileInfo.setFile_name(fileName);
                jarFileInfo.setCreate_time(DateUtil.formatTime(new Timestamp(new Date().getTime())));
                jarFileInfo.setOwner(owner);
                jarFileMapper.insert(jarFileInfo);

                ZdhNginx zdhNginx = zdhNginxMapper.selectByOwner(owner);
                File tempFile = new File(zdhNginx.getTmp_dir() + "/" + owner + "/" + fileName);
                File fileDir = new File(zdhNginx.getTmp_dir() + "/" + owner);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                String nginx_dir = zdhNginx.getNginx_dir();
                try {
                    FileCopyUtils.copy(jar_file.getInputStream(), Files.newOutputStream(tempFile.toPath()));
                    if (!zdhNginx.getHost().equals("")) {
                        System.out.println("通过sftp上传文件");
                        SFTPUtil sftp = new SFTPUtil(zdhNginx.getUsername(), zdhNginx.getPassword(),
                                zdhNginx.getHost(), new Integer(zdhNginx.getPort()));
                        sftp.login();
                        InputStream is = new FileInputStream(tempFile);
                        sftp.upload(nginx_dir + "/" + owner + "/", fileName, is);
                        sftp.logout();
                    }
                    jarFileInfo.setStatus("success");
                    jarFileMapper.updateByPrimaryKey(jarFileInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            }

        }


        JSONObject json = new JSONObject();

        json.put("success", "200");
        return json.toJSONString();
    }


    @RequestMapping("/etl_task_jar_del_file")
    @ResponseBody
    public String etl_task_jar_del_file(String[] ids, HttpServletRequest request) {
        String json_str = JSON.toJSONString(request.getParameterMap());
        String owner = getUser().getId();

        List<JarFileInfo> jarFileInfos= jarFileMapper.selectByParams(owner,ids);
        ZdhNginx zdhNginx = zdhNginxMapper.selectByOwner(owner);
        String nginx_dir = zdhNginx.getNginx_dir();
        for(JarFileInfo jarFileInfo:jarFileInfos){
            String fileName=jarFileInfo.getFile_name();
            if (!zdhNginx.getHost().equals("")) {
                System.out.println("通过sftp删除文件");
                SFTPUtil sftp = new SFTPUtil(zdhNginx.getUsername(), zdhNginx.getPassword(),
                        zdhNginx.getHost(), new Integer(zdhNginx.getPort()));
                sftp.login();

                try {
                    sftp.delete(nginx_dir + "/" + owner + "/", fileName);
                } catch (SftpException e) {
                    e.printStackTrace();
                }
                sftp.logout();
            }else{
                File tempFile = new File(zdhNginx.getTmp_dir() + "/" + owner + "/" + fileName);
                if(tempFile.exists()){
                    tempFile.delete();
                }
            }
            jarFileMapper.deleteByPrimaryKey(jarFileInfo.getId());
        }


        JSONObject json = new JSONObject();

        json.put("success", "200");
        return json.toJSONString();
    }

    @RequestMapping("/etl_task_jar_file_list")
    @ResponseBody
    public List<JarFileInfo> etl_task_jar_file_list(String id, HttpServletRequest request) {
        String json_str = JSON.toJSONString(request.getParameterMap());
        String owner = getUser().getId();
        JarFileInfo jarFileInfo = new JarFileInfo();
        jarFileInfo.setOwner(owner);
        jarFileInfo.setJar_etl_id(id);
        List<JarFileInfo> jarFileInfos=jarFileMapper.select(jarFileInfo);

        return jarFileInfos;
    }


    /**
     * ssh任务明细
     * @param ssh_context
     * @param id
     * @return
     */
    @RequestMapping(value = "/etl_task_ssh_list", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String etl_task_ssh_list(String ssh_context, String id) {

        List<SshTaskInfo> sshTaskInfos = new ArrayList<>();

        sshTaskInfos = sshTaskMapper.selectByParams(getUser().getId(), ssh_context, id);

        return JSON.toJSONString(sshTaskInfos);
    }

    /**
     * 删除ssh任务
     * @param ids
     * @return
     */
    @RequestMapping("/etl_task_ssh_delete")
    @ResponseBody
    public String etl_task_ssh_delete(String[] ids) {

        for (String id : ids)
            sshTaskMapper.deleteByPrimaryKey(id);

        JSONObject json = new JSONObject();
        json.put("success", "200");
        return json.toJSONString();
    }

    /**
     * 新增ssh任务
     * @param sshTaskInfo
     * @param jar_files
     * @return
     */
    @RequestMapping("/etl_task_ssh_add")
    @ResponseBody
    public String etl_task_ssh_add(SshTaskInfo sshTaskInfo,MultipartFile[] jar_files) {
        //String json_str=JSON.toJSONString(request.getParameterMap());
        String owner = getUser().getId();
        sshTaskInfo.setOwner(owner);
        debugInfo(sshTaskInfo);
        String id=SnowflakeIdWorker.getInstance().nextId() + "";
        sshTaskInfo.setId(id);
        sshTaskInfo.setCreate_time(new Timestamp(new Date().getTime()));


        sshTaskMapper.insert(sshTaskInfo);


        if (sshTaskInfo.getUpdate_context() != null && !sshTaskInfo.getUpdate_context().equals("")) {
            //插入更新日志表
            EtlTaskUpdateLogs etlTaskUpdateLogs = new EtlTaskUpdateLogs();
            etlTaskUpdateLogs.setId(sshTaskInfo.getId());
            etlTaskUpdateLogs.setUpdate_context(sshTaskInfo.getUpdate_context());
            etlTaskUpdateLogs.setUpdate_time(new Timestamp(new Date().getTime()));
            etlTaskUpdateLogs.setOwner(getUser().getId());
            etlTaskUpdateLogsMapper.insert(etlTaskUpdateLogs);
        }

        if (jar_files != null && jar_files.length > 0) {
            for (MultipartFile jar_file : jar_files) {
                String fileName = jar_file.getOriginalFilename();
                if(fileName==null||fileName.trim().equalsIgnoreCase("")){
                    continue;
                }
                System.out.println("上传文件不为空:"+fileName);
                JarFileInfo jarFileInfo = new JarFileInfo();
                jarFileInfo.setId(SnowflakeIdWorker.getInstance().nextId() + "");
                jarFileInfo.setJar_etl_id(id);
                jarFileInfo.setFile_name(fileName);
                jarFileInfo.setCreate_time(DateUtil.formatTime(new Timestamp(new Date().getTime())));
                jarFileInfo.setOwner(owner);
                jarFileMapper.insert(jarFileInfo);

                ZdhNginx zdhNginx = zdhNginxMapper.selectByOwner(owner);
                File tempFile = new File(zdhNginx.getTmp_dir() + "/" + owner + "/" + fileName);
                File fileDir = new File(zdhNginx.getTmp_dir() + "/" + owner);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                String nginx_dir = zdhNginx.getNginx_dir();
                try {
                    FileCopyUtils.copy(jar_file.getInputStream(), Files.newOutputStream(tempFile.toPath()));
                    if (!zdhNginx.getHost().equals("")) {
                        System.out.println("通过sftp上传文件");
                        SFTPUtil sftp = new SFTPUtil(zdhNginx.getUsername(), zdhNginx.getPassword(),
                                zdhNginx.getHost(), new Integer(zdhNginx.getPort()));
                        sftp.login();
                        InputStream is = new FileInputStream(tempFile);
                        sftp.upload(nginx_dir + "/" + owner + "/", fileName, is);
                        sftp.logout();
                    }
                    jarFileInfo.setStatus("success");
                    jarFileMapper.updateByPrimaryKey(jarFileInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            }


        }


        //dataSourcesServiceImpl.insert(dataSourcesInfo);

        JSONObject json = new JSONObject();

        json.put("success", "200");
        return json.toJSONString();
    }

    /**
     * 更新ssh任务
     * @param sshTaskInfo
     * @param jar_files
     * @return
     */
    @RequestMapping("/etl_task_ssh_update")
    @ResponseBody
    public String sql_task_update(SshTaskInfo sshTaskInfo,MultipartFile[] jar_files) {
        //String json_str=JSON.toJSONString(request.getParameterMap());
        String owner = getUser().getId();
        sshTaskInfo.setOwner(owner);
        debugInfo(sshTaskInfo);
        String id=sshTaskInfo.getId();
        sshTaskMapper.updateByPrimaryKey(sshTaskInfo);

        SshTaskInfo sti = sshTaskMapper.selectByPrimaryKey(sshTaskInfo.getId());

        if (sshTaskInfo.getUpdate_context() != null && !sshTaskInfo.getUpdate_context().equals("")
                && !sshTaskInfo.getUpdate_context().equals(sti.getUpdate_context())) {
            //插入更新日志表
            EtlTaskUpdateLogs etlTaskUpdateLogs = new EtlTaskUpdateLogs();
            etlTaskUpdateLogs.setId(sshTaskInfo.getId());
            etlTaskUpdateLogs.setUpdate_context(sshTaskInfo.getUpdate_context());
            etlTaskUpdateLogs.setUpdate_time(new Timestamp(new Date().getTime()));
            etlTaskUpdateLogs.setOwner(getUser().getId());
            etlTaskUpdateLogsMapper.insert(etlTaskUpdateLogs);
        }

        if (jar_files != null && jar_files.length > 0) {
            for (MultipartFile jar_file : jar_files) {
                String fileName = jar_file.getOriginalFilename();
                if(fileName.isEmpty()){
                    System.out.println("上传文件名称为空"+fileName);
                    continue;
                }
                System.out.println("上传文件不为空"+fileName);
                JarFileInfo jarFileInfo = new JarFileInfo();
                jarFileInfo.setId(SnowflakeIdWorker.getInstance().nextId() + "");
                jarFileInfo.setJar_etl_id(id);
                jarFileInfo.setFile_name(fileName);
                jarFileInfo.setCreate_time(DateUtil.formatTime(new Timestamp(new Date().getTime())));
                jarFileInfo.setOwner(owner);
                jarFileMapper.insert(jarFileInfo);

                ZdhNginx zdhNginx = zdhNginxMapper.selectByOwner(owner);
                File tempFile = new File(zdhNginx.getTmp_dir() + "/" + owner + "/" + fileName);
                File fileDir = new File(zdhNginx.getTmp_dir() + "/" + owner);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                System.out.println("=================="+tempFile.getAbsolutePath());
                String nginx_dir = zdhNginx.getNginx_dir();
                try {
                    FileCopyUtils.copy(jar_file.getInputStream(), Files.newOutputStream(tempFile.toPath()));
                    if (!zdhNginx.getHost().equals("")) {
                        System.out.println("通过sftp上传文件");
                        SFTPUtil sftp = new SFTPUtil(zdhNginx.getUsername(), zdhNginx.getPassword(),
                                zdhNginx.getHost(), new Integer(zdhNginx.getPort()));
                        sftp.login();
                        InputStream is = new FileInputStream(tempFile);
                        sftp.upload(nginx_dir + "/" + owner + "/", fileName, is);
                        sftp.logout();
                    }
                    jarFileInfo.setStatus("success");
                    jarFileMapper.updateByPrimaryKey(jarFileInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SftpException e) {
                    e.printStackTrace();
                }
            }

        }

        //dataSourcesServiceImpl.insert(dataSourcesInfo);

        JSONObject json = new JSONObject();

        json.put("success", "200");
        return json.toJSONString();
    }

    /**
     * ssh任务删除文件
     * @param ids
     * @param request
     * @return
     */
    @RequestMapping("/etl_task_ssh_del_file")
    @ResponseBody
    public String etl_task_ssh_del_file(String[] ids, HttpServletRequest request) {
        String json_str = JSON.toJSONString(request.getParameterMap());
        String owner = getUser().getId();

        List<JarFileInfo> jarFileInfos= jarFileMapper.selectByParams(owner,ids);
        ZdhNginx zdhNginx = zdhNginxMapper.selectByOwner(owner);
        String nginx_dir = zdhNginx.getNginx_dir();
        for(JarFileInfo jarFileInfo:jarFileInfos){
            String fileName=jarFileInfo.getFile_name();
            if (!zdhNginx.getHost().equals("")) {
                System.out.println("通过sftp删除文件");
                SFTPUtil sftp = new SFTPUtil(zdhNginx.getUsername(), zdhNginx.getPassword(),
                        zdhNginx.getHost(), new Integer(zdhNginx.getPort()));
                sftp.login();

                try {
                    sftp.delete(nginx_dir + "/" + owner + "/", fileName);
                } catch (SftpException e) {
                    e.printStackTrace();
                }
                sftp.logout();
            }else{
                File tempFile = new File(zdhNginx.getTmp_dir() + "/" + owner + "/" + fileName);
                if(tempFile.exists()){
                    tempFile.delete();
                }
            }
            jarFileMapper.deleteByPrimaryKey(jarFileInfo.getId());
        }


        JSONObject json = new JSONObject();

        json.put("success", "200");
        return json.toJSONString();
    }

    /**
     * ssh任务已上传文件明细
     * @param id
     * @param request
     * @return
     */
    @RequestMapping("/etl_task_ssh_file_list")
    @ResponseBody
    public List<JarFileInfo> etl_task_ssh_file_list(String id, HttpServletRequest request) {
        String json_str = JSON.toJSONString(request.getParameterMap());
        String owner = getUser().getId();
        JarFileInfo jarFileInfo = new JarFileInfo();
        jarFileInfo.setOwner(owner);
        jarFileInfo.setJar_etl_id(id);
        List<JarFileInfo> jarFileInfos=jarFileMapper.select(jarFileInfo);

        return jarFileInfos;
    }



    private void debugInfo(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (int i = 0, len = fields.length; i < len; i++) {
            // 对于每个属性，获取属性名
            String varName = fields[i].getName();
            try {
                // 获取原来的访问控制权限
                boolean accessFlag = fields[i].isAccessible();
                // 修改访问控制权限
                fields[i].setAccessible(true);
                // 获取在对象f中属性fields[i]对应的对象中的变量
                Object o;
                try {
                    o = fields[i].get(obj);
                    System.err.println("传入的对象中包含一个如下的变量：" + varName + " = " + o);
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // 恢复访问控制权限
                fields[i].setAccessible(accessFlag);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
    }

}
