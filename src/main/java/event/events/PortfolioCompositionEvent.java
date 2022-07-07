package event.events;

import java.time.Instant;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import event.sequencing.processing.CoreEventType;
import finance.instruments.IInstrument;
import finance.instruments.IPortfolio;

@Component
@Scope("prototype")
@Lazy(true)
public class PortfolioCompositionEvent extends Event {
	private IPortfolio portfolio;
	private IInstrument member;
	private double weight;
	
	public PortfolioCompositionEvent(Instant eventTimestamp,IPortfolio portfolio,IInstrument member) {
		super(eventTimestamp,CoreEventType.PORTFOLIO_COMPOSITION);		
		this.setPriority(Ordered.HIGHEST_PRECEDENCE);
		this.setPortfolio(portfolio);
		this.setMember(member);
	}
	
	public PortfolioCompositionEvent(Instant eventTimestamp,IPortfolio portfolio,IInstrument member,double weight) {
		super(eventTimestamp,CoreEventType.PORTFOLIO_COMPOSITION);		
		this.setPriority(Ordered.HIGHEST_PRECEDENCE);
		this.setPortfolio(portfolio);
		this.setMember(member);
		this.setWeight(weight);
	}

	public IPortfolio getPortfolio() {
		return portfolio;
	}

	public void setPortfolio(IPortfolio portfolio) {
		this.portfolio = portfolio;
	}

	public IInstrument getMember() {
		return member;
	}

	public void setMember(IInstrument member) {
		this.member = member;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
	
}
