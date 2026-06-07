package com.chatbot.module.chat.service;

import com.chatbot.dao.entity.KnowledgeDoc;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocalAiService {
    
    private static final Map<String, String> responses = new HashMap<>();
    
    static {
        responses.put("订单", "您可以通过【我的订单】页面查询订单状态和物流信息。如有问题，请提供订单号以便我为您查询。");
        responses.put("物流", "物流信息一般在发货后24小时内更新，您可以在订单详情页查看最新物流状态。");
        responses.put("退款", "退款申请一般在3-7个工作日内处理完成，退款将原路返回您的支付账户。");
        responses.put("人工", "正在为您转接人工客服，请稍候...");
        responses.put("客服", "请问有什么可以帮助您的？如需人工服务，请说\"转人工\"。");
        responses.put("价格", "商品价格可能会因促销活动而变化，建议您关注我们的优惠活动。");
        responses.put("优惠", "我们不定期举办各种优惠活动，您可以关注首页轮播图获取最新优惠信息。");
        responses.put("问题", "非常抱歉给您带来不便，请详细描述您的问题，我会尽力帮助您解决。");
        responses.put("投诉", "感谢您的反馈，我们会认真处理您的投诉，并在24小时内给您回复。");
        responses.put("发货", "订单发货后我们会第一时间通知您，请留意短信或APP推送消息。");
        responses.put("收货", "请在签收前检查商品是否完好，如有问题请当场拍照并联系客服。");
        responses.put("退货", "如需退货，请确保商品完好无损并保留原包装，在7天内申请退货。");
        responses.put("换货", "换货流程与退货类似，请在申请时选择\"换货\"选项并说明原因。");
        responses.put("发票", "电子发票将在订单完成后发送至您的邮箱，纸质发票将随商品一同寄出。");
        responses.put("保修", "我们提供7天无理由退换货服务，部分商品享有更长的保修期限。");
    }

    /**
     * 简单响应生成（不使用知识库）
     */
    public String generateResponse(String message) {
        for (Map.Entry<String, String> entry : responses.entrySet()) {
            if (message.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return "您好！我是智能客服助手，很高兴为您服务。请问有什么可以帮助您的？\n\n" +
               "常见问题：\n" +
               "- 如何查询订单状态？\n" +
               "- 如何申请退款？\n" +
               "- 如何联系人工客服？\n" +
               "- 服务时间是什么时候？";
    }

    /**
     * 使用知识库内容生成响应
     */
    public String generateResponseWithKnowledge(String message, List<KnowledgeDoc> knowledgeDocs) {
        // 首先尝试匹配预设响应
        for (Map.Entry<String, String> entry : responses.entrySet()) {
            if (message.contains(entry.getKey())) {
                String baseResponse = entry.getValue();
                
                // 如果有知识库内容，尝试结合
                if (knowledgeDocs != null && !knowledgeDocs.isEmpty()) {
                    StringBuilder kbInfo = new StringBuilder();
                    kbInfo.append("\n\n根据知识库信息：");
                    
                    int count = 0;
                    for (KnowledgeDoc doc : knowledgeDocs) {
                        if (count >= 2) break; // 最多取2条知识
                        kbInfo.append("\n【").append(doc.getTitle()).append("】");
                        String content = doc.getSummary() != null ? doc.getSummary() : doc.getContent();
                        if (content.length() > 100) {
                            content = content.substring(0, 100) + "...";
                        }
                        kbInfo.append(content);
                        count++;
                    }
                    
                    return baseResponse + kbInfo.toString();
                }
                
                return baseResponse;
            }
        }
        
        // 如果没有预设响应但有知识库内容
        if (knowledgeDocs != null && !knowledgeDocs.isEmpty()) {
            StringBuilder response = new StringBuilder();
            response.append("根据知识库内容，我来为您解答：\n\n");
            
            for (KnowledgeDoc doc : knowledgeDocs) {
                response.append("【").append(doc.getTitle()).append("】\n");
                String content = doc.getSummary() != null ? doc.getSummary() : doc.getContent();
                response.append(content).append("\n\n");
            }
            
            response.append("如果您还有其他问题，请继续提问。");
            return response.toString();
        }
        
        // 默认响应
        return "您好！我是智能客服助手，很高兴为您服务。请问有什么可以帮助您的？\n\n" +
               "常见问题：\n" +
               "- 如何查询订单状态？\n" +
               "- 如何申请退款？\n" +
               "- 如何联系人工客服？\n" +
               "- 服务时间是什么时候？";
    }
}
