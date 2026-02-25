# 华芬社团管理系统增强设计文档

> 文档版本: 1.0
> 创建日期: 2026-02-24
> 状态: 已批准

---

## 目录

1. [系统架构概述](#1-系统架构概述)
2. [问卷系统](#2-问卷系统)
3. [AI面试系统](#3-ai面试系统核心)
4. [薪酬系统](#4-薪酬系统)
5. [配置中心](#5-配置中心)
6. [权限系统与日志](#6-权限系统与日志)

---

## 1. 系统架构概述

### 1.1 技术栈

| 层级 | 技术选型 | 版本 |
|------|----------|------|
| 前端框架 | Vue 3 + TypeScript | 3.4+ |
| UI组件库 | Element Plus | 2.5+ |
| 状态管理 | Pinia | 2.x |
| 后端框架 | Spring Boot | 3.2+ |
| ORM | Spring Data JPA | 3.2+ |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis | 7.0+ |
| 实时通信 | WebSocket (STOMP) | - |
| AI集成 | OpenAI-compatible + Claude | - |

### 1.2 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        Frontend (Vue 3)                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐ │
│  │ 问卷设计  │  │ AI面试   │  │ 薪酬管理  │  │ 配置中心/权限   │ │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │   HTTP / WebSocket │
                    └─────────┬─────────┘
                              │
┌─────────────────────────────────────────────────────────────────┐
│                     Backend (Spring Boot 3.x)                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Security Filter Chain                  │   │
│  │              (JWT Authentication + RBAC)                  │   │
│  └──────────────────────────────────────────────────────────┘   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐ │
│  │Controller│  │ Service  │  │Repository│  │  AI Provider     │ │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
    ┌─────┴─────┐       ┌─────┴─────┐       ┌─────┴─────┐
    │   MySQL   │       │   Redis   │       │ AI APIs   │
    │  (主存储)  │       │  (缓存)   │       │(OpenAI等) │
    └───────────┘       └───────────┘       └───────────┘
```

### 1.3 AI多供应商支持

```java
public interface AIProvider {
    String getName();
    Flux<String> streamChat(List<ChatMessage> messages, AIConfig config);
    CompletableFuture<String> chat(List<ChatMessage> messages, AIConfig config);
}

@Component
public class OpenAICompatibleProvider implements AIProvider {
    @Override
    public String getName() { return "openai-compatible"; }

    @Override
    public Flux<String> streamChat(List<ChatMessage> messages, AIConfig config) {
        return webClient.post()
            .uri(config.getBaseUrl() + "/v1/chat/completions")
            .header("Authorization", "Bearer " + config.getApiKey())
            .bodyValue(buildRequest(messages, config))
            .retrieve()
            .bodyToFlux(String.class)
            .filter(line -> line.startsWith("data: "))
            .map(this::extractContent);
    }
}

@Component
public class ClaudeProvider implements AIProvider {
    @Override
    public String getName() { return "claude"; }

    @Override
    public Flux<String> streamChat(List<ChatMessage> messages, AIConfig config) {
        return webClient.post()
            .uri("https://api.anthropic.com/v1/messages")
            .header("x-api-key", config.getApiKey())
            .header("anthropic-version", "2023-06-01")
            .bodyValue(buildClaudeRequest(messages, config))
            .retrieve()
            .bodyToFlux(String.class)
            .filter(line -> line.contains("content_block_delta"))
            .map(this::extractClaudeContent);
    }
}
```

---

## 2. 问卷系统

### 2.1 数据模型

#### 2.1.1 问卷主表 (questionnaire)

```sql
CREATE TABLE questionnaire (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '问卷标题',
    description TEXT COMMENT '问卷描述',
    type ENUM('APPLICATION', 'SURVEY', 'FEEDBACK') NOT NULL COMMENT '问卷类型',
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT' COMMENT '状态',
    version INT DEFAULT 1 COMMENT '版本号',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开链接',
    public_token VARCHAR(64) UNIQUE COMMENT '公开访问令牌',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    max_responses INT COMMENT '最大回复数',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_public_token (public_token),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷表';
```

#### 2.1.2 问卷字段表 (questionnaire_field)

```sql
CREATE TABLE questionnaire_field (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    questionnaire_id BIGINT NOT NULL COMMENT '问卷ID',
    parent_id BIGINT COMMENT '父字段ID(用于GROUP类型)',
    field_key VARCHAR(50) NOT NULL COMMENT '字段标识',
    field_type ENUM('SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TEXT', 'DATE', 'NUMBER', 'DROPDOWN', 'GROUP') NOT NULL,
    label VARCHAR(200) NOT NULL COMMENT '字段标签',
    placeholder VARCHAR(200) COMMENT '占位提示',
    options JSON COMMENT '选项配置 [{"value":"A","label":"选项A"}]',
    validation_rules JSON COMMENT '验证规则 {"required":true,"min":1,"max":100}',
    conditional_logic JSON COMMENT '条件逻辑 {"show_when":{"field":"q1","value":"A"}}',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_required BOOLEAN DEFAULT FALSE COMMENT '是否必填',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (questionnaire_id) REFERENCES questionnaire(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES questionnaire_field(id) ON DELETE CASCADE,
    INDEX idx_questionnaire (questionnaire_id),
    INDEX idx_sort (questionnaire_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷字段表';
```

#### 2.1.3 问卷回复表 (questionnaire_response)

```sql
CREATE TABLE questionnaire_response (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    questionnaire_id BIGINT NOT NULL COMMENT '问卷ID',
    questionnaire_version INT NOT NULL COMMENT '问卷版本',
    respondent_id BIGINT COMMENT '回复人ID(匿名时为空)',
    respondent_name VARCHAR(50) COMMENT '回复人姓名',
    respondent_contact VARCHAR(100) COMMENT '联系方式',
    answers JSON NOT NULL COMMENT '回答内容 {"field_key":"value"}',
    source ENUM('APPLICATION', 'PUBLIC_LINK') NOT NULL COMMENT '来源',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '浏览器信息',
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (questionnaire_id) REFERENCES questionnaire(id),
    INDEX idx_questionnaire (questionnaire_id),
    INDEX idx_respondent (respondent_id),
    INDEX idx_submitted (submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问卷回复表';
```

### 2.2 字段类型定义

| 类型 | 说明 | 配置示例 |
|------|------|----------|
| SINGLE_CHOICE | 单选 | `{"options":[{"value":"1","label":"选项1"}]}` |
| MULTIPLE_CHOICE | 多选 | `{"options":[...],"max_select":3}` |
| TEXT | 文本 | `{"max_length":500,"multiline":true}` |
| DATE | 日期 | `{"format":"YYYY-MM-DD","min_date":"2024-01-01"}` |
| NUMBER | 数字 | `{"min":0,"max":100,"decimal":2}` |
| DROPDOWN | 下拉 | `{"options":[...],"searchable":true}` |
| GROUP | 分组 | `{"repeatable":true,"max_items":5}` |

### 2.3 前端拖拽设计器

```vue
<template>
  <div class="questionnaire-designer">
    <!-- 左侧组件面板 -->
    <div class="component-panel">
      <draggable :list="fieldTypes" :group="{name:'fields',pull:'clone',put:false}"
                 :clone="cloneField" item-key="type">
        <template #item="{element}">
          <div class="field-item">
            <el-icon><component :is="element.icon"/></el-icon>
            <span>{{ element.label }}</span>
          </div>
        </template>
      </draggable>
    </div>

    <!-- 中间设计区域 -->
    <div class="design-area">
      <draggable v-model="formFields" group="fields" item-key="id"
                 @change="onFieldChange" handle=".drag-handle">
        <template #item="{element, index}">
          <div class="field-wrapper" :class="{active: activeField?.id === element.id}"
               @click="selectField(element)">
            <el-icon class="drag-handle"><Rank/></el-icon>
            <field-preview :field="element"/>
            <el-icon class="delete-btn" @click.stop="removeField(index)"><Delete/></el-icon>
          </div>
        </template>
      </draggable>
    </div>

    <!-- 右侧属性面板 -->
    <div class="property-panel" v-if="activeField">
      <field-property-editor v-model="activeField" @update="updateField"/>
    </div>
  </div>
</template>

<script setup lang="ts">
import draggable from 'vuedraggable'
import { ref } from 'vue'
import type { QuestionnaireField } from '@/types/questionnaire'

const formFields = ref<QuestionnaireField[]>([])
const activeField = ref<QuestionnaireField | null>(null)

const fieldTypes = [
  { type: 'SINGLE_CHOICE', label: '单选题', icon: 'CircleCheck' },
  { type: 'MULTIPLE_CHOICE', label: '多选题', icon: 'Finished' },
  { type: 'TEXT', label: '文本题', icon: 'Edit' },
  { type: 'DATE', label: '日期', icon: 'Calendar' },
  { type: 'NUMBER', label: '数字', icon: 'Odometer' },
  { type: 'DROPDOWN', label: '下拉选择', icon: 'ArrowDown' },
  { type: 'GROUP', label: '题目分组', icon: 'Folder' }
]

const cloneField = (original: any): QuestionnaireField => ({
  id: Date.now(),
  fieldKey: `field_${Date.now()}`,
  fieldType: original.type,
  label: original.label,
  options: original.type.includes('CHOICE') || original.type === 'DROPDOWN'
    ? [{ value: '1', label: '选项1' }] : undefined,
  isRequired: false,
  sortOrder: formFields.value.length
})
</script>
```

### 2.4 条件逻辑配置

```typescript
interface ConditionalLogic {
  show_when?: {
    field: string;        // 依赖字段key
    operator: 'eq' | 'neq' | 'contains' | 'gt' | 'lt';
    value: any;
  };
  hide_when?: {
    field: string;
    operator: string;
    value: any;
  };
}

// 前端条件判断
const evaluateCondition = (logic: ConditionalLogic, formData: Record<string, any>): boolean => {
  if (!logic.show_when && !logic.hide_when) return true;

  const condition = logic.show_when || logic.hide_when;
  const fieldValue = formData[condition!.field];

  let result = false;
  switch (condition!.operator) {
    case 'eq': result = fieldValue === condition!.value; break;
    case 'neq': result = fieldValue !== condition!.value; break;
    case 'contains': result = fieldValue?.includes(condition!.value); break;
    case 'gt': result = Number(fieldValue) > Number(condition!.value); break;
    case 'lt': result = Number(fieldValue) < Number(condition!.value); break;
  }

  return logic.show_when ? result : !result;
}
```

### 2.5 权限控制

```java
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'LEADER', 'VICE_LEADER')")
@PostMapping("/questionnaire")
public Result<QuestionnaireVO> createQuestionnaire(@RequestBody QuestionnaireDTO dto) {
    return Result.success(questionnaireService.create(dto));
}

@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'LEADER', 'VICE_LEADER')")
@PutMapping("/questionnaire/{id}")
public Result<QuestionnaireVO> updateQuestionnaire(@PathVariable Long id, @RequestBody QuestionnaireDTO dto) {
    return Result.success(questionnaireService.update(id, dto));
}
```

---

## 3. AI面试系统（核心）

### 3.1 系统提示词中的8条群规

```java
public class InterviewSystemPrompt {
    public static final String GROUP_RULES = """
        你是一个正在接受社团面试的申请者。面试官会考察你对以下群规的理解和遵守意愿：

        【群规一】禁止发布广告、推广链接或任何商业内容
        【群规二】禁止人身攻击、辱骂或歧视性言论
        【群规三】禁止发布政治敏感、色情、暴力等违规内容
        【群规四】禁止频繁刷屏或发送无意义消息
        【群规五】禁止未经允许私自拉人进群
        【群规六】禁止冒充管理员或其他成员
        【群规七】禁止传播未经证实的谣言或虚假信息
        【群规八】禁止在群内进行任何形式的金钱交易
        """;
}
```

### 3.2 9种违规类型

```java
public enum ViolationType {
    ADVERTISING("广告推广", "我之前在其他群发过一些推广，效果还不错..."),
    PERSONAL_ATTACK("人身攻击", "有时候遇到杠精真的忍不住想骂回去..."),
    SENSITIVE_CONTENT("敏感内容", "偶尔转发一些新闻应该没问题吧？"),
    SPAM("刷屏行为", "表情包斗图很有趣啊，大家都喜欢..."),
    UNAUTHORIZED_INVITE("私拉人员", "我可以拉几个朋友进来吗？他们人都很好..."),
    IMPERSONATION("冒充身份", "开玩笑说自己是管理员应该没事吧？"),
    RUMOR_SPREADING("传播谣言", "我看到的消息就转发了，没核实过..."),
    MONEY_TRANSACTION("金钱交易", "群里收个二手应该可以吧？"),
    RULE_QUESTIONING("质疑规则", "这些规则是不是太严格了？")
}
```

### 3.3 AI行为模式

```java
public class AIBehaviorConfig {
    // 违规倾向随机选择1-2个
    public static List<ViolationType> selectViolations() {
        List<ViolationType> all = Arrays.asList(ViolationType.values());
        Collections.shuffle(all);
        int count = ThreadLocalRandom.current().nextInt(1, 3); // 1-2个
        return all.subList(0, count);
    }

    // 行为阶段：抵触 -> 借口 -> 情绪化 -> 逐渐服从
    public static String getBehaviorPrompt(int messageCount, List<ViolationType> violations) {
        String violationDesc = violations.stream()
            .map(v -> v.getDescription())
            .collect(Collectors.joining("、"));

        if (messageCount <= 3) {
            return String.format("""
                你对以下方面有违规倾向：%s
                当前阶段：【抵触期】
                - 对规则表示不理解或质疑
                - 认为自己的行为没什么问题
                - 语气略带不耐烦
                """, violationDesc);
        } else if (messageCount <= 6) {
            return String.format("""
                你对以下方面有违规倾向：%s
                当前阶段：【借口期】
                - 开始找各种理由为自己辩解
                - "我不是故意的"、"别人也这样做"
                - 试图讨价还价
                """, violationDesc);
        } else if (messageCount <= 9) {
            return String.format("""
                你对以下方面有违规倾向：%s
                当前阶段：【情绪期】
                - 可能表现出委屈或不满
                - "为什么针对我"、"我又没做什么过分的事"
                - 但开始有所动摇
                """, violationDesc);
        } else {
            return String.format("""
                你对以下方面有违规倾向：%s
                当前阶段：【服从期】
                - 逐渐理解规则的必要性
                - 表示愿意遵守
                - 态度变得诚恳
                """, violationDesc);
        }
    }
}
```

### 3.4 数据模型

#### 3.4.1 面试会话表 (ai_interview_session)

```sql
CREATE TABLE ai_interview_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    application_id BIGINT NOT NULL COMMENT '申请ID',
    interviewer_id BIGINT NOT NULL COMMENT '面试官ID',
    ai_provider VARCHAR(50) NOT NULL COMMENT 'AI供应商',
    ai_model VARCHAR(100) NOT NULL COMMENT 'AI模型',
    violations JSON NOT NULL COMMENT '本次违规倾向 ["ADVERTISING","SPAM"]',
    status ENUM('IN_PROGRESS', 'COMPLETED', 'ABANDONED') DEFAULT 'IN_PROGRESS',
    started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    ended_at DATETIME,
    total_messages INT DEFAULT 0 COMMENT '消息总数',
    INDEX idx_application (application_id),
    INDEX idx_interviewer (interviewer_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI面试会话表';
```

#### 3.4.2 面试消息表 (ai_interview_message)

```sql
CREATE TABLE ai_interview_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL COMMENT '会话ID',
    role ENUM('INTERVIEWER', 'AI_APPLICANT', 'SYSTEM') NOT NULL COMMENT '角色',
    content TEXT NOT NULL COMMENT '消息内容',
    tokens_used INT COMMENT '消耗token数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES ai_interview_session(id) ON DELETE CASCADE,
    INDEX idx_session (session_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI面试消息表';
```

#### 3.4.3 面试评分表 (ai_interview_score)

```sql
CREATE TABLE ai_interview_score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL UNIQUE COMMENT '会话ID',
    rule_understanding INT NOT NULL COMMENT '规则理解(0-25)',
    communication_skill INT NOT NULL COMMENT '沟通能力(0-25)',
    attitude INT NOT NULL COMMENT '态度表现(0-25)',
    compliance_willingness INT NOT NULL COMMENT '服从意愿(0-25)',
    total_score INT GENERATED ALWAYS AS (rule_understanding + communication_skill + attitude + compliance_willingness) STORED,
    comment TEXT COMMENT '评语',
    scored_by BIGINT NOT NULL COMMENT '评分人ID',
    scored_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES ai_interview_session(id) ON DELETE CASCADE,
    INDEX idx_total_score (total_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI面试评分表';
```

### 3.5 评分维度说明

| 维度 | 分值 | 评分标准 |
|------|------|----------|
| 规则理解 | 0-25 | 是否准确理解群规内容和目的 |
| 沟通能力 | 0-25 | 表达是否清晰、逻辑是否通顺 |
| 态度表现 | 0-25 | 是否尊重面试官、态度是否端正 |
| 服从意愿 | 0-25 | 最终是否表示愿意遵守规则 |

### 3.6 WebSocket实时通信

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/interview")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}

@Controller
public class InterviewWebSocketController {

    @Autowired
    private AIInterviewService interviewService;

    @MessageMapping("/interview/{sessionId}/send")
    public void sendMessage(@DestinationVariable Long sessionId,
                           @Payload InterviewMessageDTO message,
                           Principal principal) {
        // 保存面试官消息
        interviewService.saveInterviewerMessage(sessionId, message.getContent());

        // 流式返回AI响应
        interviewService.streamAIResponse(sessionId, message.getContent())
            .subscribe(chunk -> {
                messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/interview/" + sessionId,
                    new StreamChunk(chunk)
                );
            });
    }
}
```

### 3.7 前端聊天界面

```vue
<template>
  <div class="interview-chat">
    <!-- 聊天消息区域 -->
    <div class="message-list" ref="messageListRef">
      <div v-for="msg in messages" :key="msg.id"
           :class="['message', msg.role.toLowerCase()]">
        <el-avatar :icon="msg.role === 'INTERVIEWER' ? 'User' : 'Service'" />
        <div class="message-content">
          <div class="message-text" v-html="renderMarkdown(msg.content)"></div>
          <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
        </div>
      </div>
      <!-- 流式响应显示 -->
      <div v-if="streamingContent" class="message ai_applicant">
        <el-avatar icon="Service" />
        <div class="message-content">
          <div class="message-text">{{ streamingContent }}<span class="cursor">|</span></div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <el-input v-model="inputMessage" type="textarea" :rows="3"
                placeholder="输入消息..." :disabled="isStreaming"
                @keydown.enter.ctrl="sendMessage" />
      <el-button type="primary" @click="sendMessage" :loading="isStreaming">
        发送
      </el-button>
    </div>

    <!-- 评分面板 -->
    <el-drawer v-model="showScorePanel" title="面试评分" size="400px">
      <el-form :model="scoreForm" label-width="100px">
        <el-form-item label="规则理解">
          <el-slider v-model="scoreForm.ruleUnderstanding" :max="25" show-input />
        </el-form-item>
        <el-form-item label="沟通能力">
          <el-slider v-model="scoreForm.communicationSkill" :max="25" show-input />
        </el-form-item>
        <el-form-item label="态度表现">
          <el-slider v-model="scoreForm.attitude" :max="25" show-input />
        </el-form-item>
        <el-form-item label="服从意愿">
          <el-slider v-model="scoreForm.complianceWillingness" :max="25" show-input />
        </el-form-item>
        <el-form-item label="总分">
          <el-tag type="primary" size="large">{{ totalScore }} / 100</el-tag>
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="scoreForm.comment" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitScore">提交评分</el-button>
        </el-form-item>
      </el-form>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const messages = ref<InterviewMessage[]>([])
const inputMessage = ref('')
const streamingContent = ref('')
const isStreaming = ref(false)
let stompClient: Client | null = null

const connectWebSocket = (sessionId: number) => {
  stompClient = new Client({
    webSocketFactory: () => new SockJS('/ws/interview'),
    onConnect: () => {
      stompClient?.subscribe(`/user/queue/interview/${sessionId}`, (message) => {
        const chunk = JSON.parse(message.body)
        if (chunk.done) {
          messages.value.push({ role: 'AI_APPLICANT', content: streamingContent.value })
          streamingContent.value = ''
          isStreaming.value = false
        } else {
          streamingContent.value += chunk.content
        }
      })
    }
  })
  stompClient.activate()
}

const sendMessage = () => {
  if (!inputMessage.value.trim() || isStreaming.value) return
  messages.value.push({ role: 'INTERVIEWER', content: inputMessage.value })
  stompClient?.publish({
    destination: `/app/interview/${sessionId}/send`,
    body: JSON.stringify({ content: inputMessage.value })
  })
  inputMessage.value = ''
  isStreaming.value = true
}

const totalScore = computed(() =>
  scoreForm.value.ruleUnderstanding + scoreForm.value.communicationSkill +
  scoreForm.value.attitude + scoreForm.value.complianceWillingness
)
</script>
```

### 3.8 评分雷达图

```vue
<template>
  <div class="score-radar">
    <v-chart :option="radarOption" autoresize style="height: 300px" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'

const props = defineProps<{ score: InterviewScore }>()

const radarOption = computed(() => ({
  radar: {
    indicator: [
      { name: '规则理解', max: 25 },
      { name: '沟通能力', max: 25 },
      { name: '态度表现', max: 25 },
      { name: '服从意愿', max: 25 }
    ],
    shape: 'polygon'
  },
  series: [{
    type: 'radar',
    data: [{
      value: [
        props.score.ruleUnderstanding,
        props.score.communicationSkill,
        props.score.attitude,
        props.score.complianceWillingness
      ],
      name: '面试评分',
      areaStyle: { opacity: 0.3 }
    }]
  }]
}))
</script>
```

---

## 4. 薪酬系统

### 4.1 薪酬规则

#### 4.1.1 薪酬池分配

| 项目 | 数值 |
|------|------|
| 月度总池 | 2000 迷你币 |
| 正式成员数 | 5 人 |
| 人均范围 | 200-400 迷你币 |

#### 4.1.2 打卡积分规则

| 打卡次数 | 积分变化 |
|----------|----------|
| < 20 次 | -20 分 |
| 20-29 次 | -10 分 |
| 30-39 次 | 0 分 |
| 40-49 次 | +30 分 |
| ≥ 50 次 | +50 分 |

#### 4.1.3 成员流动规则

```
实习生 → 正式成员: 累计积分 ≥ 100
正式成员 → 实习生: 连续2个月积分 < 150
```

### 4.2 数据模型

#### 4.2.1 增强薪酬表 (salary)

```sql
CREATE TABLE salary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    year_month VARCHAR(7) NOT NULL COMMENT '年月 2026-02',
    checkin_count INT DEFAULT 0 COMMENT '打卡次数',
    checkin_points INT DEFAULT 0 COMMENT '打卡积分',
    task_points INT DEFAULT 0 COMMENT '任务积分',
    bonus_points INT DEFAULT 0 COMMENT '奖励积分',
    penalty_points INT DEFAULT 0 COMMENT '惩罚积分',
    total_points INT GENERATED ALWAYS AS (checkin_points + task_points + bonus_points - penalty_points) STORED,
    base_salary INT DEFAULT 0 COMMENT '基础薪酬',
    performance_salary INT DEFAULT 0 COMMENT '绩效薪酬',
    total_salary INT GENERATED ALWAYS AS (base_salary + performance_salary) STORED,
    status ENUM('DRAFT', 'CONFIRMED', 'PAID') DEFAULT 'DRAFT',
    confirmed_by BIGINT COMMENT '确认人',
    confirmed_at DATETIME COMMENT '确认时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_month (user_id, year_month),
    INDEX idx_year_month (year_month),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪酬表';
```

#### 4.2.2 成员流动日志表 (member_flow_log)

```sql
CREATE TABLE member_flow_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    from_role VARCHAR(50) NOT NULL COMMENT '原角色',
    to_role VARCHAR(50) NOT NULL COMMENT '新角色',
    reason VARCHAR(200) NOT NULL COMMENT '变动原因',
    trigger_type ENUM('AUTO', 'MANUAL') NOT NULL COMMENT '触发类型',
    triggered_by BIGINT COMMENT '操作人(手动时)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成员流动日志表';
```

#### 4.2.3 月度绩效表 (monthly_performance)

```sql
CREATE TABLE monthly_performance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    year_month VARCHAR(7) NOT NULL COMMENT '年月',
    accumulated_points INT DEFAULT 0 COMMENT '累计积分',
    consecutive_low_months INT DEFAULT 0 COMMENT '连续低分月数',
    member_status ENUM('INTERN', 'MEMBER') NOT NULL COMMENT '成员状态',
    status_changed BOOLEAN DEFAULT FALSE COMMENT '本月是否变动',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_month (user_id, year_month),
    INDEX idx_status (member_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月度绩效表';
```

### 4.3 薪酬计算服务

```java
@Service
public class SalaryCalculationService {

    private static final int TOTAL_POOL = 2000;
    private static final int MIN_SALARY = 200;
    private static final int MAX_SALARY = 400;

    public int calculateCheckinPoints(int checkinCount) {
        if (checkinCount < 20) return -20;
        if (checkinCount < 30) return -10;
        if (checkinCount < 40) return 0;
        if (checkinCount < 50) return 30;
        return 50;
    }

    public void calculateMonthlySalary(String yearMonth) {
        List<Salary> salaries = salaryRepository.findByYearMonth(yearMonth);
        List<User> formalMembers = userRepository.findByRole("MEMBER");

        // 计算总积分
        int totalPoints = salaries.stream()
            .filter(s -> formalMembers.stream().anyMatch(m -> m.getId().equals(s.getUserId())))
            .mapToInt(Salary::getTotalPoints)
            .sum();

        // 按积分比例分配薪酬
        for (Salary salary : salaries) {
            if (formalMembers.stream().noneMatch(m -> m.getId().equals(salary.getUserId()))) {
                continue; // 实习生不参与薪酬分配
            }

            double ratio = (double) salary.getTotalPoints() / totalPoints;
            int calculated = (int) (TOTAL_POOL * ratio);

            // 限制在200-400范围内
            salary.setPerformanceSalary(Math.max(MIN_SALARY, Math.min(MAX_SALARY, calculated)));
            salaryRepository.save(salary);
        }
    }

    @Scheduled(cron = "0 0 0 1 * ?") // 每月1号执行
    public void checkMemberFlow() {
        String lastMonth = YearMonth.now().minusMonths(1).toString();
        List<MonthlyPerformance> performances = performanceRepository.findByYearMonth(lastMonth);

        for (MonthlyPerformance perf : performances) {
            User user = userRepository.findById(perf.getUserId()).orElse(null);
            if (user == null) continue;

            // 实习生升级检查
            if ("INTERN".equals(user.getRole()) && perf.getAccumulatedPoints() >= 100) {
                promoteToMember(user, perf);
            }
            // 正式成员降级检查
            else if ("MEMBER".equals(user.getRole()) && perf.getTotalPoints() < 150) {
                perf.setConsecutiveLowMonths(perf.getConsecutiveLowMonths() + 1);
                if (perf.getConsecutiveLowMonths() >= 2) {
                    demoteToIntern(user, perf);
                }
            } else if ("MEMBER".equals(user.getRole())) {
                perf.setConsecutiveLowMonths(0); // 重置连续低分月数
            }
        }
    }
}
```

### 4.4 前端可编辑表格

```vue
<template>
  <div class="salary-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ currentMonth }} 薪酬管理</span>
          <div>
            <el-button @click="recalculate" :loading="calculating">重新计算</el-button>
            <el-button type="primary" @click="confirmAll" :disabled="!canConfirm">确认发放</el-button>
          </div>
        </div>
      </template>

      <el-table :data="salaryList" border v-loading="loading">
        <el-table-column prop="userName" label="成员" width="120" />
        <el-table-column prop="memberStatus" label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.memberStatus === 'MEMBER' ? 'success' : 'info'">
              {{ row.memberStatus === 'MEMBER' ? '正式' : '实习' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkinCount" label="打卡次数" width="100">
          <template #default="{row}">
            <el-input-number v-model="row.checkinCount" :min="0" :max="100"
                            size="small" @change="onCheckinChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="checkinPoints" label="打卡积分" width="100">
          <template #default="{row}">
            <el-tag :type="row.checkinPoints >= 0 ? 'success' : 'danger'">
              {{ row.checkinPoints > 0 ? '+' : '' }}{{ row.checkinPoints }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="taskPoints" label="任务积分" width="100">
          <template #default="{row}">
            <el-input-number v-model="row.taskPoints" :min="0" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="bonusPoints" label="奖励" width="100">
          <template #default="{row}">
            <el-input-number v-model="row.bonusPoints" :min="0" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="penaltyPoints" label="惩罚" width="100">
          <template #default="{row}">
            <el-input-number v-model="row.penaltyPoints" :min="0" size="small" />
          </template>
        </el-table-column>
        <el-table-column prop="totalPoints" label="总积分" width="100">
          <template #default="{row}">
            <span :class="{'text-danger': row.totalPoints < 0}">{{ row.totalPoints }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalSalary" label="薪酬" width="120">
          <template #default="{row}">
            <span v-if="row.memberStatus === 'MEMBER'">{{ row.totalSalary }} 迷你币</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <!-- 薪酬池统计 -->
      <div class="pool-summary">
        <el-statistic title="薪酬池" :value="2000" suffix="迷你币" />
        <el-statistic title="已分配" :value="allocatedTotal" suffix="迷你币" />
        <el-statistic title="剩余" :value="2000 - allocatedTotal" suffix="迷你币"
                     :value-style="{color: remainingColor}" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const salaryList = ref<SalaryRecord[]>([])

const onCheckinChange = (row: SalaryRecord) => {
  // 实时计算打卡积分
  const count = row.checkinCount
  if (count < 20) row.checkinPoints = -20
  else if (count < 30) row.checkinPoints = -10
  else if (count < 40) row.checkinPoints = 0
  else if (count < 50) row.checkinPoints = 30
  else row.checkinPoints = 50

  row.totalPoints = row.checkinPoints + row.taskPoints + row.bonusPoints - row.penaltyPoints
}

const allocatedTotal = computed(() =>
  salaryList.value
    .filter(s => s.memberStatus === 'MEMBER')
    .reduce((sum, s) => sum + s.totalSalary, 0)
)

const remainingColor = computed(() =>
  2000 - allocatedTotal.value < 0 ? '#F56C6C' : '#67C23A'
)
</script>
```

---

## 5. 配置中心

### 5.1 配置分类

| 分类 | 说明 | 配置项示例 |
|------|------|------------|
| AI | AI服务配置 | api_key, base_url, model, temperature |
| OSS | 对象存储配置 | endpoint, access_key, secret_key, bucket |
| Email | 邮件服务配置 | smtp_host, smtp_port, username, password |
| System | 系统配置 | site_name, logo_url, max_upload_size |

### 5.2 数据模型

```sql
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    encrypted_value VARBINARY(1024) COMMENT '加密值',
    is_encrypted BOOLEAN DEFAULT FALSE COMMENT '是否加密',
    category ENUM('AI', 'OSS', 'EMAIL', 'SYSTEM') NOT NULL COMMENT '分类',
    description VARCHAR(500) COMMENT '描述',
    value_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';
```

### 5.3 AES-256加密服务

```java
@Service
public class ConfigEncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    @Value("${app.config.encryption-key}")
    private String encryptionKey;

    public byte[] encrypt(String plainText) throws Exception {
        SecretKey key = new SecretKeySpec(
            Base64.getDecoder().decode(encryptionKey), "AES");

        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom.getInstanceStrong().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // IV + 密文
        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

        return result;
    }

    public String decrypt(byte[] encryptedData) throws Exception {
        SecretKey key = new SecretKeySpec(
            Base64.getDecoder().decode(encryptionKey), "AES");

        byte[] iv = Arrays.copyOfRange(encryptedData, 0, GCM_IV_LENGTH);
        byte[] encrypted = Arrays.copyOfRange(encryptedData, GCM_IV_LENGTH, encryptedData.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }
}
```

### 5.4 Redis缓存与热加载

```java
@Service
public class ConfigCacheService {

    private static final String CONFIG_CACHE_PREFIX = "config:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SystemConfigRepository configRepository;

    @Autowired
    private ConfigEncryptionService encryptionService;

    public String getConfig(String key) {
        String cacheKey = CONFIG_CACHE_PREFIX + key;

        // 先从Redis获取
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 从数据库获取
        SystemConfig config = configRepository.findByConfigKey(key)
            .orElseThrow(() -> new ConfigNotFoundException(key));

        String value;
        if (config.getIsEncrypted()) {
            try {
                value = encryptionService.decrypt(config.getEncryptedValue());
            } catch (Exception e) {
                throw new ConfigDecryptException(key, e);
            }
        } else {
            value = config.getConfigValue();
        }

        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, value, CACHE_TTL);
        return value;
    }

    public void updateConfig(String key, String value, boolean encrypt) {
        SystemConfig config = configRepository.findByConfigKey(key)
            .orElse(new SystemConfig());

        config.setConfigKey(key);
        config.setIsEncrypted(encrypt);

        if (encrypt) {
            try {
                config.setEncryptedValue(encryptionService.encrypt(value));
                config.setConfigValue(null);
            } catch (Exception e) {
                throw new ConfigEncryptException(key, e);
            }
        } else {
            config.setConfigValue(value);
            config.setEncryptedValue(null);
        }

        configRepository.save(config);

        // 清除缓存，触发热加载
        redisTemplate.delete(CONFIG_CACHE_PREFIX + key);

        // 发布配置变更事件
        redisTemplate.convertAndSend("config:reload", key);
    }

    @EventListener
    public void onConfigReload(ConfigReloadEvent event) {
        // 热加载：清除本地缓存，下次访问时重新加载
        localCache.invalidate(event.getKey());
    }
}
```

### 5.5 连接测试服务

```java
@Service
public class ConfigTestService {

    public TestResult testAIConnection(AIConfigDTO config) {
        try {
            WebClient client = WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + config.getApiKey())
                .build();

            String response = client.post()
                .uri("/v1/chat/completions")
                .bodyValue(Map.of(
                    "model", config.getModel(),
                    "messages", List.of(Map.of("role", "user", "content", "Hello")),
                    "max_tokens", 5
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(10));

            return TestResult.success("AI连接成功");
        } catch (Exception e) {
            return TestResult.failure("AI连接失败: " + e.getMessage());
        }
    }

    public TestResult testOSSConnection(OSSConfigDTO config) {
        try {
            MinioClient client = MinioClient.builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();

            boolean exists = client.bucketExists(
                BucketExistsArgs.builder().bucket(config.getBucket()).build());

            return exists
                ? TestResult.success("OSS连接成功，Bucket存在")
                : TestResult.failure("OSS连接成功，但Bucket不存在");
        } catch (Exception e) {
            return TestResult.failure("OSS连接失败: " + e.getMessage());
        }
    }

    public TestResult testEmailConnection(EmailConfigDTO config) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", config.getSmtpHost());
            props.put("mail.smtp.port", config.getSmtpPort());
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", config.isStartTls());

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getUsername(), config.getPassword());
                }
            });

            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();

            return TestResult.success("邮件服务连接成功");
        } catch (Exception e) {
            return TestResult.failure("邮件服务连接失败: " + e.getMessage());
        }
    }
}
```

### 5.6 权限控制

```java
@RestController
@RequestMapping("/api/config")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'LEADER')")
public class ConfigController {

    @GetMapping("/category/{category}")
    public Result<List<ConfigVO>> getByCategory(@PathVariable String category) {
        return Result.success(configService.getByCategory(category));
    }

    @PutMapping("/{key}")
    @OperationLog(module = "CONFIG", action = "UPDATE")
    public Result<Void> updateConfig(@PathVariable String key, @RequestBody ConfigUpdateDTO dto) {
        configService.updateConfig(key, dto.getValue(), dto.isEncrypt());
        return Result.success();
    }

    @PostMapping("/test/ai")
    public Result<TestResult> testAI(@RequestBody AIConfigDTO config) {
        return Result.success(configTestService.testAIConnection(config));
    }

    @PostMapping("/test/oss")
    public Result<TestResult> testOSS(@RequestBody OSSConfigDTO config) {
        return Result.success(configTestService.testOSSConnection(config));
    }

    @PostMapping("/test/email")
    public Result<TestResult> testEmail(@RequestBody EmailConfigDTO config) {
        return Result.success(configTestService.testEmailConnection(config));
    }
}
```

---

## 6. 权限系统与日志

### 6.1 动态三级菜单系统

#### 6.1.1 菜单表 (menu)

```sql
CREATE TABLE menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    menu_type ENUM('DIRECTORY', 'MENU', 'BUTTON') NOT NULL COMMENT '类型',
    permission VARCHAR(100) COMMENT '权限标识',
    visible BOOLEAN DEFAULT TRUE COMMENT '是否可见',
    status BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent (parent_id),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';
```

#### 6.1.2 角色菜单关联表 (role_menu)

```sql
CREATE TABLE role_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';
```

#### 6.1.3 权限表 (permission)

```sql
CREATE TABLE permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '权限名称',
    code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    description VARCHAR(200) COMMENT '描述',
    module VARCHAR(50) COMMENT '所属模块',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_module (module),
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';
```

#### 6.1.4 角色权限关联表 (role_permission)

```sql
CREATE TABLE role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';
```

### 6.2 按钮级权限指令

```typescript
// src/directives/permission.ts
import type { Directive, DirectiveBinding } from 'vue'
import { useUserStore } from '@/stores/user'

export const vPermission: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    const userStore = useUserStore()
    const { value } = binding

    if (!value) return

    const permissions = userStore.permissions
    const required = Array.isArray(value) ? value : [value]

    const hasPermission = required.some(p => permissions.includes(p))

    if (!hasPermission) {
      el.parentNode?.removeChild(el)
    }
  }
}

// 使用示例
// <el-button v-permission="'user:delete'" type="danger">删除</el-button>
// <el-button v-permission="['user:edit', 'user:update']">编辑</el-button>
```

### 6.3 变更日志表

#### 6.3.1 角色变更日志 (role_change_log)

```sql
CREATE TABLE role_change_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '被变更用户ID',
    old_role VARCHAR(50) COMMENT '原角色',
    new_role VARCHAR(50) COMMENT '新角色',
    reason VARCHAR(500) COMMENT '变更原因',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_operator (operator_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色变更日志表';
```

#### 6.3.2 权限变更日志 (permission_change_log)

```sql
CREATE TABLE permission_change_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    change_type ENUM('GRANT', 'REVOKE') NOT NULL COMMENT '变更类型',
    permission_ids JSON NOT NULL COMMENT '权限ID列表',
    reason VARCHAR(500) COMMENT '变更原因',
    operator_id BIGINT NOT NULL COMMENT '操作人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_role (role_id),
    INDEX idx_operator (operator_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限变更日志表';
```

### 6.4 操作日志系统

#### 6.4.1 操作日志表 (operation_log)

```sql
CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作人ID',
    username VARCHAR(50) COMMENT '操作人用户名',
    category ENUM('LOGIN', 'DELETE', 'PERMISSION', 'EMAIL', 'OSS', 'CONFIG', 'INTERVIEW', 'SALARY') NOT NULL,
    module VARCHAR(50) NOT NULL COMMENT '模块',
    action VARCHAR(50) NOT NULL COMMENT '操作',
    description VARCHAR(500) COMMENT '描述',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_data TEXT COMMENT '响应数据',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '浏览器信息',
    execution_time INT COMMENT '执行时间(ms)',
    status ENUM('SUCCESS', 'FAILURE') DEFAULT 'SUCCESS',
    error_message TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_category (category),
    INDEX idx_module (module),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
```

#### 6.4.2 日志注解与切面

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {
    String module();
    String action() default "";
    LogCategory category() default LogCategory.SYSTEM;
    boolean saveParams() default true;
    boolean saveResponse() default false;
}

@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogRepository logRepository;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        OperationLogEntity log = new OperationLogEntity();

        try {
            // 获取当前用户
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserDetails) {
                UserDetails user = (UserDetails) auth.getPrincipal();
                log.setUsername(user.getUsername());
            }

            // 获取请求信息
            HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getRequest();
            log.setRequestMethod(request.getMethod());
            log.setRequestUrl(request.getRequestURI());
            log.setIpAddress(getClientIP(request));
            log.setUserAgent(request.getHeader("User-Agent"));

            // 设置日志信息
            log.setModule(operationLog.module());
            log.setAction(operationLog.action());
            log.setCategory(operationLog.category());

            if (operationLog.saveParams()) {
                log.setRequestParams(JSON.toJSONString(point.getArgs()));
            }

            // 执行方法
            Object result = point.proceed();

            if (operationLog.saveResponse()) {
                log.setResponseData(JSON.toJSONString(result));
            }

            log.setStatus(LogStatus.SUCCESS);
            return result;

        } catch (Exception e) {
            log.setStatus(LogStatus.FAILURE);
            log.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            log.setExecutionTime((int) (System.currentTimeMillis() - startTime));
            logRepository.save(log);
        }
    }
}
```

### 6.5 面包屑导航

```vue
<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/' }">
      <el-icon><HomeFilled /></el-icon>
    </el-breadcrumb-item>
    <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path"
                        :to="item.path ? { path: item.path } : undefined">
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useMenuStore } from '@/stores/menu'

const route = useRoute()
const menuStore = useMenuStore()

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title as string
  }))
})
</script>
```

### 6.6 动态路由生成

```typescript
// src/router/dynamic.ts
import type { RouteRecordRaw } from 'vue-router'
import type { MenuItem } from '@/types/menu'

const modules = import.meta.glob('@/views/**/*.vue')

export function generateRoutes(menus: MenuItem[]): RouteRecordRaw[] {
  const routes: RouteRecordRaw[] = []

  for (const menu of menus) {
    if (menu.menuType === 'BUTTON') continue

    const route: RouteRecordRaw = {
      path: menu.path || '',
      name: menu.name,
      meta: {
        title: menu.name,
        icon: menu.icon,
        permission: menu.permission
      },
      component: menu.component
        ? modules[`/src/views/${menu.component}.vue`]
        : () => import('@/layouts/RouteView.vue'),
      children: menu.children?.length
        ? generateRoutes(menu.children)
        : undefined
    }

    routes.push(route)
  }

  return routes
}

// 路由守卫中动态添加
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  if (userStore.token && !userStore.menuLoaded) {
    const menus = await userStore.fetchMenus()
    const dynamicRoutes = generateRoutes(menus)

    dynamicRoutes.forEach(route => {
      router.addRoute('Layout', route)
    })

    userStore.menuLoaded = true
    next({ ...to, replace: true })
  } else {
    next()
  }
})
```

---

## 附录

### A. 初始化数据

```sql
-- 初始化菜单
INSERT INTO menu (id, parent_id, name, path, component, icon, sort_order, menu_type, permission) VALUES
(1, 0, '系统管理', '/system', NULL, 'Setting', 1, 'DIRECTORY', NULL),
(2, 1, '用户管理', '/system/user', 'system/user/index', 'User', 1, 'MENU', 'system:user:list'),
(3, 1, '角色管理', '/system/role', 'system/role/index', 'UserFilled', 2, 'MENU', 'system:role:list'),
(4, 1, '菜单管理', '/system/menu', 'system/menu/index', 'Menu', 3, 'MENU', 'system:menu:list'),
(5, 1, '配置中心', '/system/config', 'system/config/index', 'Tools', 4, 'MENU', 'system:config:list'),
(6, 1, '操作日志', '/system/log', 'system/log/index', 'Document', 5, 'MENU', 'system:log:list'),
(7, 0, '社团管理', '/club', NULL, 'OfficeBuilding', 2, 'DIRECTORY', NULL),
(8, 7, '申请管理', '/club/application', 'club/application/index', 'Tickets', 1, 'MENU', 'club:application:list'),
(9, 7, 'AI面试', '/club/interview', 'club/interview/index', 'ChatDotRound', 2, 'MENU', 'club:interview:list'),
(10, 7, '薪酬管理', '/club/salary', 'club/salary/index', 'Money', 3, 'MENU', 'club:salary:list'),
(11, 7, '问卷管理', '/club/questionnaire', 'club/questionnaire/index', 'List', 4, 'MENU', 'club:questionnaire:list'),
(12, 2, '新增用户', NULL, NULL, NULL, 1, 'BUTTON', 'system:user:add'),
(13, 2, '编辑用户', NULL, NULL, NULL, 2, 'BUTTON', 'system:user:edit'),
(14, 2, '删除用户', NULL, NULL, NULL, 3, 'BUTTON', 'system:user:delete');

-- 初始化权限
INSERT INTO permission (id, name, code, description, module) VALUES
(1, '用户列表', 'system:user:list', '查看用户列表', 'system'),
(2, '新增用户', 'system:user:add', '新增用户', 'system'),
(3, '编辑用户', 'system:user:edit', '编辑用户', 'system'),
(4, '删除用户', 'system:user:delete', '删除用户', 'system'),
(5, '角色列表', 'system:role:list', '查看角色列表', 'system'),
(6, '配置管理', 'system:config:list', '查看配置列表', 'system'),
(7, '配置修改', 'system:config:edit', '修改配置', 'system'),
(8, '日志查看', 'system:log:list', '查看操作日志', 'system'),
(9, '申请列表', 'club:application:list', '查看申请列表', 'club'),
(10, '面试管理', 'club:interview:list', '查看面试列表', 'club'),
(11, '薪酬管理', 'club:salary:list', '查看薪酬列表', 'club'),
(12, '问卷管理', 'club:questionnaire:list', '查看问卷列表', 'club');
```

### B. 日志分类说明

| 分类 | 说明 | 记录场景 |
|------|------|----------|
| LOGIN | 登录日志 | 用户登录、登出、登录失败 |
| DELETE | 删除操作 | 任何删除操作 |
| PERMISSION | 权限变更 | 角色分配、权限修改 |
| EMAIL | 邮件操作 | 发送邮件、邮件配置变更 |
| OSS | 存储操作 | 文件上传、删除 |
| CONFIG | 配置变更 | 系统配置修改 |
| INTERVIEW | 面试操作 | 面试创建、评分 |
| SALARY | 薪酬操作 | 薪酬计算、确认发放 |

---

> 文档结束
