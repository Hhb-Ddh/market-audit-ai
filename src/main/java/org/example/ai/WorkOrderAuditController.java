package org.example.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkOrderAuditController {

    // 1. 加载 .env 文件，拿到安全的 API Key
    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_KEY = dotenv.get("SILICONFLOW_API_KEY");

    // 2. 你的超强 Prompt 原封不动塞进去
    private final String SYSTEM_PROMPT = """
            角色：你是一个市场监管局违规工单智能审核助手
                        
            任务：识别工单是否合规“校验必填字段是否存在空值、无效值；必填项：工单编号、诉求人联系方式、被投诉主体（名称 / 统一社会信用代码 / 地址至少一项）、投诉事项、发生时间、诉求类型；区分【缺失】【格式错误】，不要笼统标记不全”、是否属于本局受理范围“1. 区分市场监管职责范围：价格、广告、食品、特种设备、计量、商标、反不正当竞争、消费纠纷等；2. 排除不属于市监职责：劳资纠纷、邻里纠纷、公安治安、物业权属、医疗诊疗纠纷等；3. 校验属地：经营地址是否在本局管辖辖区；输出结论：本局受理 / 转其他部门 / 跨区域移送”等
                        
            格式：请严格输出 JSON，不要有任何多余解释。{
                "basic_info": {
                    "work_order_id": "string",
                    "complainant_phone": "string",
                    "complained_subject": "string",
                    "occur_time": "string",
                    "demand_type": "string"
                },
                "element_check_result": {
                    "is_complete": "bool",
                    "missing_fields": ["string"],
                    "format_error_fields": ["string"],
                    "check_basis": "string"
                },
                "jurisdiction_result": {
                    "accept_type": "enum【本局受理 / 移送其他部门 / 跨区域移送】",
                    "accept_reason": "string",
                    "transfer_target": "string"
                },
                "duplicate_check_result": {
                    "is_duplicate": "bool",
                    "duplicate_type": "enum【无重复 / 完全重复 / 近似重复】",
                    "related_work_order_ids": ["string"]
                },
                "invalid_order_check_result": {
                    "is_invalid": "bool",
                    "invalid_type": "enum【有效工单 / 无实质诉求 / 恶意刷诉 / 辱骂内容 / 信息虚假】",
                    "invalid_reason": "string"
                },
                "violation_tag_result": {
                    "violation_tags": ["string"],
                    "tag_basis": "string"
                },
                "risk_level": "enum【低风险 / 中风险 / 高风险】",
                "review_suggestion": "string",
                "need_manual_review": "bool",
                "manual_review_note": "string",
                "tip": "智能初审结果，仅供审核人员参考，最终以人工审核为准"
                }
            }
                        
                        
            样例：输入：{
                "work_order_id": "12315-20261103-00125",
                "complainant_phone": "13800138000",
                "complained_subject": "潍坊市XX零食店",
                "unified_social_credit_code": "",
                "business_address": "潍坊市XX区XX街道XX路12号",
                "occur_time": "2026-11-01",
                "demand_type": "消费投诉",
                "order_content": "本人昨天在该店铺购买袋装面包，打开发现已经发霉变质，商家拒绝退款，要求监管部门查处该店售卖过期变质食品，并要求退款赔偿。",
                "known_related_order_ids": []
            }
            ，输出：{
                "basic_info": {
                    "work_order_id": "12315-20261103-00125",
                    "complainant_phone": "13800138000",
                    "complained_subject": "潍坊市XX零食店",
                    "occur_time": "2026-11-01",
                    "demand_type": "消费投诉"
                },
                "element_check_result": {
                    "is_complete": true,
                    "missing_fields": [],
                    "format_error_fields": [],
                    "check_basis": "工单包含诉求人电话、被投诉主体、地址、事发时间、投诉事项、诉求类型，必填要素齐全"
                },
                "jurisdiction_result": {
                    "accept_type": "本局受理",
                    "accept_reason": "被投诉主体经营地址属于本局辖区，售卖变质食品属于市场监管食品安全监管职责",
                    "transfer_target": ""
                },
                "duplicate_check_result": {
                    "is_duplicate": false,
                    "duplicate_type": "无重复",
                    "related_work_order_ids": []
                },
                "invalid_order_check_result": {
                    "is_invalid": false,
                    "invalid_type": "有效工单",
                    "invalid_reason": ""
                },
                "violation_tag_result": {
                    "violation_tags": ["食品安全-销售变质食品"],
                    "tag_basis": "工单原文：购买袋装面包，打开发现已经发霉变质"
                },
                "risk_level": "中风险",
                "review_suggestion": "工单要素齐全、属于本局管辖，涉嫌销售变质食品，建议分派执法人员现场核查，同步联系诉求人核实证据",
                "need_manual_review": false,
                "manual_review_note": "",
                "tip": "智能初审结果，仅供审核人员参考，最终以人工审核为准"
            }""";

    @PostMapping("/api/audit")
    public String auditWorkOrder(@RequestBody String userInput) {
        // 3. 初始化大模型（从 .env 中读取 API_KEY）
        ChatLanguageModel model = OpenAiChatModel.builder()
                .baseUrl("https://api.siliconflow.cn/v1")
                .apiKey(API_KEY) // 安全引用
                .modelName("Qwen/Qwen2.5-7B-Instruct")
                .build();

        // 4. 用 <<< 和 >>> 作为分隔符，防止用户输入里含有恶意指令
        String finalPrompt = SYSTEM_PROMPT + "\n\n用户输入：\n<<<\n" + userInput + "\n>>>";

        // 5. 调大模型并返回结果
        return model.generate(finalPrompt);
    }
}