/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.entity.CopyHistoryTaskEntity;
import org.activiti.engine.impl.persistence.entity.CopyTaskEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ReadUserNames;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.query.NativeQuery;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Event;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;

import com.frameworkset.util.ListInfo;

/** Service which provides access to {@link Task} and form related operations.
 * 
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public interface TaskService {
  public static final int  op_rejected = 3;
  public static final int  op_withdraw = 1;
  public static final int  op_jump = 2;
  public static final int  op_returntorejected = 0;
	/**
	 * Creates a new task that is not related to any process instance.
	 * 
	 * The returned task is transient and must be saved with {@link #saveTask(Task)} 'manually'.
	 */
  Task newTask();
  
  /** create a new task with a user defined task id */
  Task newTask(String taskId);
	
	/**
	 * Saves the given task to the persistent data store. If the task is already
	 * present in the persistent store, it is updated.
	 * After a new task has been saved, the task instance passed into this method
	 * is updated with the id of the newly created task.
	 * @param task the task, cannot be null.
	 */
	void saveTask(Task task);
	
	/**
	 * Deletes the given task, not deleting historic information that is related to this task.
	 * @param taskId The id of the task that will be deleted, cannot be null. If no task
	 * exists with the given taskId, the operation is ignored.
	 * @throws ActivitiObjectNotFoundException when the task with given id does not exist.
	 * @throws ActivitiException when an error occurs while deleting the task or in case the task is part
   *   of a running process.
	 */
	void deleteTask(String taskId);
	
	/**
	 * Deletes all tasks of the given collection, not deleting historic information that is related 
	 * to these tasks.
	 * @param taskIds The id's of the tasks that will be deleted, cannot be null. All
	 * id's in the list that don't have an existing task will be ignored.
	 * @throws ActivitiObjectNotFoundException when one of the task does not exist.
	 * @throws ActivitiException when an error occurs while deleting the tasks or in case one of the tasks
   *  is part of a running process.
	 */
	void deleteTasks(Collection<String> taskIds);
	
  /**
   * Deletes the given task.
   * @param taskId The id of the task that will be deleted, cannot be null. If no task
   * exists with the given taskId, the operation is ignored.
   * @param cascade If cascade is true, also the historic information related to this task is deleted.
   * @throws ActivitiObjectNotFoundException when the task with given id does not exist.
   * @throws ActivitiException when an error occurs while deleting the task or in case the task is part
   *   of a running process.
   */
  void deleteTask(String taskId, boolean cascade);
  
  /**
   * Deletes all tasks of the given collection.
   * @param taskIds The id's of the tasks that will be deleted, cannot be null. All
   * id's in the list that don't have an existing task will be ignored.
   * @param cascade If cascade is true, also the historic information related to this task is deleted.
   * @throws ActivitiObjectNotFoundException when one of the tasks does not exist.
   * @throws ActivitiException when an error occurs while deleting the tasks or in case one of the tasks
   *  is part of a running process.
   */
  void deleteTasks(Collection<String> taskIds, boolean cascade);
  
  /**
   * Deletes the given task, not deleting historic information that is related to this task..
   * @param taskId The id of the task that will be deleted, cannot be null. If no task
   * exists with the given taskId, the operation is ignored.
   * @param deleteReason reason the task is deleted. Is recorded in history, if enabled.
   * @throws ActivitiObjectNotFoundException when the task with given id does not exist.
   * @throws ActivitiException when an error occurs while deleting the task or in case the task is part
   *  of a running process
   */
  void deleteTask(String taskId, String deleteReason);
  
  /**
   * Deletes all tasks of the given collection, not deleting historic information that is related to these tasks.
   * @param taskIds The id's of the tasks that will be deleted, cannot be null. All
   * id's in the list that don't have an existing task will be ignored.
   * @param deleteReason reason the task is deleted. Is recorded in history, if enabled.
   * @throws ActivitiObjectNotFoundException when one of the tasks does not exist.
   * @throws ActivitiException when an error occurs while deleting the tasks or in case one of the tasks
   *  is part of a running process.
   */
  void deleteTasks(Collection<String> taskIds, String deleteReason);
  
	 /**
   * Claim responsibility for a task: the given user is made assignee for the task.
   * The difference with {@link #setAssignee(String, String)} is that here 
   * a check is done if the task already has a user assigned to it.
   * No check is done whether the user is known by the identity component.
   * @param taskId task to claim, cannot be null.
   * @param userId user that claims the task. When userId is null the task is unclaimed,
   * assigned to no one.
   * @throws ActivitiObjectNotFoundException when the task doesn't exist.
   * @throws ActivitiTaskAlreadyClaimedException when the task is already claimed by another user.
   */
  void claim(String taskId, String userId);
  
  /**
   * Called when the task is successfully executed.
   * @param taskId the id of the task to complete, cannot be null.
   * @throws ActivitiObjectNotFoundException when no task exists with the given id.
   * @throws ActivitiException when this task is {@link DelegationState#PENDING} delegation.
   */
  void complete(String taskId);
  void completeWithReason(String taskId,String completeReason,String bussinessop,String bussinessRemark);
  
  /**
   * Called when the task is successfully executed.
   * @param taskId the id of the task to complete, cannot be null.
   * @param destinationTaskKey the  destination taskKey of the task where trans to, if be null see method complete(String taskId).
   * @throws ActivitiObjectNotFoundException when no task exists with the given id.
   * @throws ActivitiException when this task is {@link DelegationState#PENDING} delegation.
   */
  void completeWithDest(String taskId,String destinationTaskKey);
  void completeWithDestReason(String taskId,String destinationTaskKey,String completeReason,String bussinessop,String bussinessRemark);
  /**
   * 用于任务驳回操作
   * @param taskId the id of the task to complete, cannot be null.
   * @param variables the  destination taskKey of the task where trans to, if be null see method complete(String taskId).
   * @param rejected true 表示驳回操作，false表示正常完成任务
   */
  void complete(String taskId, Map<String, Object> variables,int op);
  void completeWithReason(String taskId, Map<String, Object> variables,int op,String reason,String bussinessop,String bussinessRemark);
  /**
   * Delegates the task to another user. This means that the assignee is set 
   * and the delegation state is set to {@link DelegationState#PENDING}.
   * If no owner is set on the task, the owner is set to the current assignee
   * of the task.
   * @param taskId The id of the task that will be delegated.
   * @param userId The id of the user that will be set as assignee.
   * @throws ActivitiObjectNotFoundException when no task exists with the given id.
   */
  void delegateTask(String taskId, String userId);
  
  /**
   * Marks that the assignee is done with this task and that it can be send back to the owner.  
   * Can only be called when this task is {@link DelegationState#PENDING} delegation.
   * After this method returns, the {@link Task#getDelegationState() delegationState} is set to {@link DelegationState#RESOLVED}.
   * @param taskId the id of the task to resolve, cannot be null.
   * @throws ActivitiObjectNotFoundException when no task exists with the given id.
   */
  void resolveTask(String taskId);

  /**
   * Called when the task is successfully executed, 
   * and the required task parameters are given by the end-user.
   * @param taskId the id of the task to complete, cannot be null.
   * @param variables task parameters. May be null or empty.
   * @throws ActivitiObjectNotFoundException when no task exists with the given id.
   */
  void complete(String taskId, Map<String, Object> variables);
  void completeWithReason(String taskId, Map<String, Object> variables,String completeReason,String bussinessop,String bussinessRemark);
  void completeWithReason(String taskId, Map<String, Object> variables,String completeReason,String bussinessop,String bussinessRemark,boolean autocomplete);
  /**
   * Called when the task is successfully executed, 
   * and the required task parameters are given by the end-user.
   * 如果下一个任务是多实例任务，
   * 那么可以通过流程变量在运行式设置多实例任务执行的方式为串行还是并行
   * 变量的命名规范为：
   * taskkey.bpmn.behavior.multiInstance.mode
   * 取值范围为：
   * 	parallel
   * 	sequential
   * 说明：taskkey为对应的任务的定义id
   * 
   * 这个变量可以在设计流程时统一配置，可以启动流程实例时动态修改，也可以在上个活动任务完成时修改
   * 
   * 在任务完成时，可以通过变量destinationTaskKey动态指定流程跳转的目标地址
   * @param taskId the id of the task to complete, cannot be null.
   * @param variables task parameters. May be null or empty.
   * @param destinationTaskKey the  destination taskKey of the task where trans to, if be null see method complete(String taskId).
   * @throws ActivitiObjectNotFoundException when no task exists with the given id.
   */
  void complete(String taskId, Map<String, Object> variables,String destinationTaskKey);
  void completeWithReason(String taskId, Map<String, Object> variables,String destinationTaskKey,String reason,String bussinessop,String bussinessRemark);
  
  
  /**
   * 将当前任务驳回到上一个任务处理人处，并更新流程变量参数
   * @param taskId
   * @param variables
 * @return 
   */
  boolean rejecttoPreTask(String taskId, Map<String, Object> variables);
  
  /**
   * 将当前任务驳回到上一个任务处理人处
   * @param taskId
   */
  boolean rejecttoPreTask(String taskId);
  
  /**
   * 将当前任务驳回到上一个任务处理人处，并更新流程变量参数
   * @param taskId
   * @param variables
 * @return 
   */
  boolean rejecttoPreTask(String taskId, Map<String, Object> variables,String rejectReason,String bussinessop,String bussinessRemark);
  
  /**
   * 将当前任务驳回到上一个任务处理人处
   * @param taskId
   */
  boolean rejecttoPreTask(String taskId,String rejectReason,String bussinessop,String bussinessRemark);


  /**
   * Changes the assignee of the given task to the given userId.
   * No check is done whether the user is known by the identity component.
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as assignee.
   * @throws ActivitiObjectNotFoundException when the task or user doesn't exist.
   */
  void setAssignee(String taskId, String userId);
  
  /**
   * Transfers ownership of this task to another user.
   * No check is done whether the user is known by the identity component.
   * @param taskId id of the task, cannot be null.
   * @param userId of the person that is receiving ownership.
   * @throws ActivitiObjectNotFoundException when the task or user doesn't exist.
   */
  void setOwner(String taskId, String userId);
  
  /**
   * Retrieves the {@link IdentityLink}s associated with the given task.
   * Such an {@link IdentityLink} informs how a certain identity (eg. group or user)
   * is associated with a certain task (eg. as candidate, assignee, etc.)
   */
  List<IdentityLink> getIdentityLinksForTask(String taskId);
  
  /**
   * Convenience shorthand for {@link #addUserIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as candidate, cannot be null.
   * @throws ActivitiObjectNotFoundException when the task or user doesn't exist.
   */
  void addCandidateUser(String taskId, String userId);
  
  /**
   * Convenience shorthand for {@link #addGroupIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to use as candidate, cannot be null.
   * @throws ActivitiObjectNotFoundException when the task or group doesn't exist.
   */
  void addCandidateGroup(String taskId, String groupId);
  
  /**
   * Involves a user with a task. The type of identity link is defined by the
   * given identityLinkType.
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user involve, cannot be null.
   * @param identityLinkType type of identityLink, cannot be null (@see {@link IdentityLinkType}).
   * @throws ActivitiObjectNotFoundException when the task or user doesn't exist.
   */
  void addUserIdentityLink(String taskId, String userId, String identityLinkType);
  
  /**
   * Involves a group with a task. The type of identityLink is defined by the
   * given identityLink.
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to involve, cannot be null.
   * @param identityLinkType type of identity, cannot be null (@see {@link IdentityLinkType}).
   * @throws ActivitiObjectNotFoundException when the task or group doesn't exist.
   */
  void addGroupIdentityLink(String taskId, String groupId, String identityLinkType);
  
  /**
   * Convenience shorthand for {@link #deleteUserIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user to use as candidate, cannot be null.
   * @throws ActivitiObjectNotFoundException when the task or user doesn't exist.
   */
  void deleteCandidateUser(String taskId, String userId);
  
  /**
   * Convenience shorthand for {@link #deleteGroupIdentityLink(String, String, String)}; with type {@link IdentityLinkType#CANDIDATE}
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to use as candidate, cannot be null.
   * @throws ActivitiObjectNotFoundException when the task or group doesn't exist.
   */
  void deleteCandidateGroup(String taskId, String groupId);
  
  /**
   * Removes the association between a user and a task for the given identityLinkType.
   * @param taskId id of the task, cannot be null.
   * @param userId id of the user involve, cannot be null.
   * @param identityLinkType type of identityLink, cannot be null (@see {@link IdentityLinkType}).
   * @throws ActivitiObjectNotFoundException when the task or user doesn't exist.
   */
  void deleteUserIdentityLink(String taskId, String userId, String identityLinkType);
  
  /**
   * Removes the association between a group and a task for the given identityLinkType.
   * @param taskId id of the task, cannot be null.
   * @param groupId id of the group to involve, cannot be null.
   * @param identityLinkType type of identity, cannot be null (@see {@link IdentityLinkType}).
   * @throws ActivitiObjectNotFoundException when the task or group doesn't exist.
   */
  void deleteGroupIdentityLink(String taskId, String groupId, String identityLinkType);
  
  /**
   * Changes the priority of the task.
   * 
   * Authorization: actual owner / business admin
   * 
   * @param taskId id of the task, cannot be null.
   * @param priority the new priority for the task.
   * @throws ActivitiObjectNotFoundException when the task doesn't exist.
   */
  void setPriority(String taskId, int priority);
  
  /**
   * Returns a new {@link TaskQuery} that can be used to dynamically query tasks.
   */
  TaskQuery createTaskQuery();
  
  /**
   * Returns a new {@link NativeQuery} for tasks.
   */
  NativeTaskQuery createNativeTaskQuery();

  /** set variable on a task.  If the variable is not already existing, it will be created in the 
   * most outer scope.  This means the process instance in case this task is related to an 
   * execution. */
  void setVariable(String taskId, String variableName, Object value);

  /** set variables on a task.  If the variable is not already existing, it will be created in the 
   * most outer scope.  This means the process instance in case this task is related to an 
   * execution. */
  void setVariables(String taskId, Map<String, ? extends Object> variables);

  /** set variable on a task.  If the variable is not already existing, it will be created in the 
   * task.  */
  void setVariableLocal(String taskId, String variableName, Object value);

  /** set variables on a task.  If the variable is not already existing, it will be created in the 
   * task.  */
  void setVariablesLocal(String taskId, Map<String, ? extends Object> variables);

  /** get a variables and search in the task scope and if available also the execution scopes. */
  Object getVariable(String taskId, String variableName);

  /** get a variables and only search in the task scope.  */
  Object getVariableLocal(String taskId, String variableName);

  /** get all variables and search in the task scope and if available also the execution scopes. 
   * If you have many variables and you only need a few, consider using {@link #getVariables(String, Collection)} 
   * for better performance.*/
  Map<String, Object> getVariables(String taskId);

  /** get all variables and search only in the task scope.
  * If you have many task local variables and you only need a few, consider using {@link #getVariablesLocal(String, Collection)} 
  * for better performance.*/
  Map<String, Object> getVariablesLocal(String taskId);

  /** get values for all given variableNames and search only in the task scope. */
  Map<String, Object> getVariables(String taskId, Collection<String> variableNames);

  /** get a variable on a task */
  Map<String, Object> getVariablesLocal(String taskId, Collection<String> variableNames);
  
  /**
   * Removes the variable from the task.
   * When the variable does not exist, nothing happens.
   */
  void removeVariable(String taskId, String variableName);

  /**
   * Removes the variable from the task (not considering parent scopes).
   * When the variable does not exist, nothing happens.
   */
  void removeVariableLocal(String taskId, String variableName);

  /**
   * Removes all variables in the given collection from the task.
   * Non existing variable names are simply ignored.
   */
  void removeVariables(String taskId, Collection<String> variableNames);

  /**
   * Removes all variables in the given collection from the task (not considering parent scopes).
   * Non existing variable names are simply ignored.
   */
  void removeVariablesLocal(String taskId, Collection<String> variableNames);

  /** Add a comment to a task and/or process instance. */
  void addComment(String taskId, String processInstanceId, String message);

  /** The comments related to the given task. */
  List<Comment> getTaskComments(String taskId);

  /** The all events related to the given task. */
  List<Event> getTaskEvents(String taskId);

  /** The comments related to the given process instance. */
  List<Comment> getProcessInstanceComments(String processInstanceId);

  /** Add a new attachment to a task and/or a process instance and use an input stream to provide the content */
  Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, InputStream content);

  /** Add a new attachment to a task and/or a process instance and use an url as the content */
  Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, String url);
  
  /** Update the name and decription of an attachment */
  void saveAttachment(Attachment attachment);
  
  /** Retrieve a particular attachment */
  Attachment getAttachment(String attachmentId);
  
  /** Retrieve stream content of a particular attachment */
  InputStream getAttachmentContent(String attachmentId);
  
  /** The list of attachments associated to a task */
  List<Attachment> getTaskAttachments(String taskId);

  /** The list of attachments associated to a process instance */
  List<Attachment> getProcessInstanceAttachments(String processInstanceId);

  /** Delete an attachment */
  void deleteAttachment(String attachmentId);

  /** The list of subtasks for this parent task */
  List<Task> getSubTasks(String parentTaskId);
  
  /**
  *
  * 将当前任务驳回到上一个任务处理人处，并更新流程变量参数
  * 如果需要改变处理人，可以通过指定变量的的方式设置
  * rejectedtype 0-驳回上一个任务对应的节点 1-驳回到当前节点的上一个节点（多条路径暂时不支持）
  * @param taskId
  * @param variables
  */
 public boolean rejecttoPreTask(String taskId, Map<String, Object> variables,int rejectedtype );
 /**
  * 
  * @param taskId
  * @param variables
  * @param rejectReason
  * @param rejectedtype 0-驳回上一个任务对应的节点 1-驳回到当前节点的上一个节点（多条路径暂时不支持）
  * @return
  */
 public boolean rejecttoPreTask(String taskId, Map<String, Object> variables,String rejectReason,int rejectedtype,String bussinessop,String bussinessRemark);
 
 
 /**
  * 将当前任务驳回到上一个任务处理人处
  * @param taskId
  * @param 0-驳回上一个任务对应的节点 1-驳回到当前节点的上一个节点（多条路径暂时不支持）
  */
 public boolean rejecttoPreTask(String taskId,int rejectedtype);
 
 /**
  * 将当前任务驳回到上一个任务处理人处
  * @param taskId
  */
 public boolean rejecttoPreTask(String taskId,String rejectReason,int rejectedtype,String bussinessop,String bussinessRemark);
 
 
 /**
 *
 * 将当前任务驳回到上一个任务处理人处，并更新流程变量参数
 * 如果需要改变处理人，可以通过指定变量的的方式设置
 * rejectedtype 0-驳回上一个任务对应的节点 1-驳回到当前节点的上一个节点（多条路径暂时不支持）
 * @param taskId
 * @param variables
 */
public boolean rejecttoTask(String taskId, Map<String, Object> variables,String desttaskkey );
/**
 * 
 * @param taskId
 * @param variables
 * @param rejectReason
 * @param rejectedtype 0-驳回上一个任务对应的节点 1-驳回到当前节点的上一个节点（多条路径暂时不支持）
 * @return
 */
public boolean rejecttoTask(String taskId, Map<String, Object> variables,String rejectReason,String desttaskkey,String bussinessop,String bussinessRemark);


/**
 * 将当前任务驳回到上一个任务处理人处
 * @param taskId
 * @param 0-驳回上一个任务对应的节点 1-驳回到当前节点的上一个节点（多条路径暂时不支持）
 */
public boolean rejecttoTask(String taskId,String desttaskkey);

/**
 * 将当前任务驳回到上一个任务处理人处
 * @param taskId
 */
public boolean rejecttoTask(String taskId,String rejectReason,String desttaskkey,String bussinessop,String bussinessRemark);
 
 /**
  * 获取当前任务的驳回节点 
  * @param taskId
  * @return 驳回节点数组，包含两个元素：第一个元素是上个任务环节对应的节点，第二个元素是当前节点的上一个节点
  */
 public String[] findRejectedNode(String taskId);
 
 /**
  * 获取当前任务对应的流程的第一个节点
  * @param taskId
  * @return 
  */
 public ActivityImpl findFirstNodeByteTask(String taskId);
 /**
  * 获取流程定义id对应的流程的第一个节点 
  * @param taskId
  * @return 驳回节点数组，包含两个元素：第一个元素是上个任务环节对应的节点，第二个元素是当前节点的上一个节点
  */
 public ActivityImpl findFirstNodeByDefID(String processdefid);
 /**
  * 获取流程定义id对应的流程的第一个节点 
  * @param taskId
  * @return 驳回节点数组，包含两个元素：第一个元素是上个任务环节对应的节点，第二个元素是当前节点的上一个节点
  */
 public ActivityImpl findFirstNodeByDefKey(String processdefKey);
 
 /**
  * 获取当前任务的驳回节点 
  * @param taskId
  * @return 驳回节点数组，包含两个元素：第一个元素是上个任务环节对应的节点，第二个元素是当前节点的上一个节点
  */
 public ActivityImpl[] findRejectedActivityNode(String taskId);
 
// public void complete( boolean returntoreject,String taskId);
// public void completeWithReason( boolean returntoreject,String taskId,String completeReason);
// public void complete( boolean returntoreject,String taskId, Map<String, Object> variables);
// public void completeWithReason(boolean returntoreject,String taskId, Map<String, Object> variables,String completeReason);
 
 /******************
  * 一组控制是否返回驳回点的控制变量
  */
 





/**
*
* 将当前任务驳回到上一个任务处理人处，并更新流程变量参数
* 如果需要改变处理人，可以通过指定变量的的方式设置
* rejectedtype 0-驳回上一个任务对应的节点 1-驳回到当前节点的上一个节点（多条路径暂时不支持）
* @param taskId
* @param variables
*/
public boolean rejecttoTask(String taskId, Map<String, Object> variables,String desttaskkey ,boolean returntoreject);
/**
* 
* @param taskId
* @param variables
* @param rejectReason
* @param rejectedtype 0-驳回上一个任务对应的节点 1-驳回到当前节点的上一个节点（多条路径暂时不支持）
* @return
*/
public boolean rejecttoTask(String taskId, Map<String, Object> variables,String rejectReason,String desttaskkey,boolean returntoreject,String bussinessop,String bussinessRemark);


/**
* 将当前任务驳回到上一个任务处理人处
* @param taskId
* @param 0-驳回上一个任务对应的节点 1-驳回到当前节点的上一个节点（多条路径暂时不支持）
*/
public boolean rejecttoTask(String taskId,String desttaskkey,boolean returntoreject);

/**
* 将当前任务驳回到上一个任务处理人处
* @param taskId
*/
public boolean rejecttoTask(String taskId,String rejectReason,String desttaskkey,boolean returntoreject,String bussinessop,String bussinessRemark);
/**
 * 撤销任务时调用的方法
 * @param taskId
 * @param variables
 * @param rejectReason
 * @param desttaskkey
 * @param returntoreject
 * @param bussinessop
 * @param bussinessRemark
 * @return
 */
public boolean withdrawTask(String taskId, Map<String, Object> variables,
		String withdrawReason, String desttaskkey,String bussinessop,String bussinessRemark);

/**
 * 创建抄送和通知任务
 * @param execution
 */
public void createCopyTasks(ExecutionEntity execution) ;
/**
 * 完成抄送/通知任务
 * @param copytaskid
 */
public void completeCopyTask(String copytaskid,String copyuser);
/**
 * 获取用户的通知和抄送任务
 * @param user 用户账号
 * @param orgs 用户隶属的机构列表
 * @param process_key
 * @param businesskey
 * @return
 */
public List<CopyTaskEntity> getUserCopyTasks(String user,List<String> orgs,String process_key,String businesskey);

/**
 * 管理员视角查看流程的阅读任务
 * @param process_key
 * @param businesskey
 * @return
 */
public List<CopyTaskEntity> getAdminCopyTasks(String process_key,String businesskey);

/**
 * 获取任务的阅读记录
 * @param actinstid 活动任务id
 * @return
 */
public List<CopyHistoryTaskEntity> getCopyTaskReadUsers(String actinstid);
/**
 * 获取根据活动任务id获取任务的阅读记录中文名称
 * @param actinstid 活动任务id
 * @return
 */
public String getCopyTaskReadUserNames(String actinstid);

/**
 * 获取根据活动任务id获取任务的阅读记录中文名称,只返回前limit个用户
 * @param actinstid 活动任务id
 * @return
 */
public ReadUserNames getCopyTaskReadUserNames(String actinstid,int limit);
/**
 * 获取根据活动任务id获取任务的阅读记录
 * @param actinstid 活动任务id
 * @return
 */
public ListInfo getCopyTaskReadUsers(String actinstid,long offeset,int pagesize);
/**
 * 获取用户的通知和抄送任务
 * @param user 用户账号
 * @param orgs 用户隶属的机构列表（父子机构）
 * @return
 */
public ListInfo getAdminCopyTasks(String process_key,String businesskey,long offeset,int pagesize);
/**
 * 获取用户的通知和抄送任务
 * @param user 用户账号
 * @param orgs 用户隶属的机构列表（父子机构）
 * @return
 */
public ListInfo getUserCopyTasks(String user,List<String> orgs,String process_key,String businesskey,long offeset,int pagesize);
/**
 * 获取用户阅读记录
 * @param actinstid 活动任务id
 * @return
 */
public ListInfo getUserReaderCopyTasks(String user,String process_key,String businesskey,long offeset,int pagesize);

/**
 * 管理员查看所有用户阅读记录
 * @param actinstid 活动任务id
 * @return
 */
public ListInfo getAdminUserReaderCopyTasks(String process_key,String businesskey,long offeset,int pagesize);
}
