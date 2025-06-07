package com.qianyv.jstartaiagent.advisors;

import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {


	private AdvisedRequest before(AdvisedRequest advisedRequest) {

		Map<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());//浅拷贝
		advisedUserParams.put("re2_input_query", advisedRequest.userText());//新增一个键值对，存放用户输入的文本

		return AdvisedRequest.from(advisedRequest)
			.userText("""
			    {re2_input_query}
			    Read the question again: {re2_input_query}
			    """)//模板字符串，{re2_input_query}的内容就是用户输入的文本
			.userParams(advisedUserParams)
			.build();
	}

	@Override
	public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
		return chain.nextAroundCall(this.before(advisedRequest));
	}

	@Override
	public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
		return chain.nextAroundStream(this.before(advisedRequest));
	}

	@Override
	public int getOrder() {
		return 0;
	}

    @Override
    public String getName() {
		return this.getClass().getSimpleName();
	}
}
