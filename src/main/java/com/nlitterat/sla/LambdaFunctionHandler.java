package com.nlitterat.sla;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.DimensionFilter;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
import com.amazonaws.services.cloudwatch.model.Metric;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.resourcegroupstaggingapi.AWSResourceGroupsTaggingAPIAsync;
import com.amazonaws.services.resourcegroupstaggingapi.AWSResourceGroupsTaggingAPIAsyncClientBuilder;
import com.amazonaws.services.resourcegroupstaggingapi.model.GetResourcesRequest;
import com.amazonaws.services.resourcegroupstaggingapi.model.GetResourcesResult;
import com.amazonaws.services.resourcegroupstaggingapi.model.ResourceTagMapping;

public class LambdaFunctionHandler implements RequestHandler<Object, String> {
	private AmazonCloudWatch amazonCloudWatch = AmazonCloudWatchClientBuilder.standard().withRegion("us-east-1")
			.build();
	private long offsetInMilliseconds = 1000 * 60 * 10 * 1; //get data 10 min back
	private LambdaLogger logger = null;

	@Override
	public String handleRequest(Object input, Context context) {
		logger = context.getLogger();

		listMetricsInfo("AWS/Route53", "HealthCheckStatus");

		return "Done";
	}

	private void listMetricsInfo(String namespace, String metricName) {

		GetResourcesResult resourceTags = getResourceTags();
		List<DimensionFilter> dimensionFilters = new ArrayList<>();
		// dimensions.add(dimensionFilter);

		ListMetricsRequest request = new ListMetricsRequest().withMetricName(metricName).withNamespace(namespace)
				.withDimensions(dimensionFilters);
		// logger.log("Request:" + request);
		boolean done = false;

		while (!done) {
			ListMetricsResult response = amazonCloudWatch.listMetrics(request);
			List<Metric> metrics = response.getMetrics();
			for (Iterator<Metric> metricsIterator = metrics.iterator(); metricsIterator.hasNext();) {
				Metric metric = (Metric) metricsIterator.next();

				List<Dimension> dimensions = metric.getDimensions();
				for (Iterator<Dimension> dimensionsIterator = dimensions.iterator(); dimensionsIterator.hasNext();) {
					Dimension dimension = (Dimension) dimensionsIterator.next();
					// logger.log("dimension:" + dimension);
					findCloudWatchData(namespace, metricName, dimension.getName(), dimension.getValue(), resourceTags);
				}
			}
			/*
			 * System.out.println("Response:" + response); for(Metric metric :
			 * response.getMetrics()) { logger.info( "Retrieved metric "+ metric); }
			 */

			request.setNextToken(response.getNextToken());

			if (response.getNextToken() == null) {
				done = true;
			}
		}
	}

	/*
	 * Read the tags which are part of the route53 health checks resources
	 */
	private GetResourcesResult getResourceTags() {
		List<String> resources = new ArrayList<>();
		resources.add("route53");
		GetResourcesRequest getResourcesRequest = new GetResourcesRequest();
		getResourcesRequest.withResourceTypeFilters(resources);

		AWSResourceGroupsTaggingAPIAsync awsResourceGroupsTaggingAPIAsync = AWSResourceGroupsTaggingAPIAsyncClientBuilder
				.standard().withRegion("us-east-1").build();
		GetResourcesResult getResourcesResult = awsResourceGroupsTaggingAPIAsync.getResources(getResourcesRequest);

		/*
		 * for (ResourceTagMapping resourceTagMapping :
		 * getResourcesResult.getResourceTagMappingList()) {
		 * logger.log("resourceTagMapping: " + resourceTagMapping);
		 * 
		 * }
		 */
		return getResourcesResult;
	}

	private void findCloudWatchData(String namespace, String metricName, String dimensionName, String dimensionValue,
			GetResourcesResult resourceTags) {

		Dimension instanceDimension = new Dimension();
		instanceDimension.setName(dimensionName);
		instanceDimension.setValue(dimensionValue);

		List<Dimension> instanceDimensions = new ArrayList<Dimension>();
		instanceDimensions.add(instanceDimension);

		GetMetricStatisticsRequest request = new GetMetricStatisticsRequest()
				.withStartTime(new Date(new Date().getTime() - offsetInMilliseconds)).withNamespace(namespace)
				.withPeriod(30).withMetricName(metricName).withStatistics("Minimum").withDimensions(instanceDimensions)
				.withEndTime(new Date());

		// logger.log("request:" + request);

		GetMetricStatisticsResult metricStatisticsResult = amazonCloudWatch.getMetricStatistics(request);
		String resourceTag = getResourceTag(resourceTags, dimensionValue);
		writeLogs(metricStatisticsResult, dimensionName, dimensionValue, resourceTag);
		// logger.log("MetricStatisticsResult:" + metricStatisticsResult);
	}

	private String getResourceTag(GetResourcesResult resourceTags, String dimensionValue) {
		Optional<ResourceTagMapping> findFirst = resourceTags.getResourceTagMappingList().stream()
				.filter(e -> e.getResourceARN().contains(dimensionValue)).findFirst();
		String tagName = findFirst.map(e -> e.getTags().stream().findFirst().get().getValue()).orElse(null);
		return tagName;

	}

	private void writeLogs(GetMetricStatisticsResult metricStatisticsResult, String dimensionName,
			String dimensionValue, String resourceTag) {
		// logger.log("Number of datapoints for "+dimensionValue+ " = " +
		// metricStatisticsResult.getDatapoints().size());
		ServiceMetric serviceMetric = new ServiceMetric();
		serviceMetric.setDimensionName(dimensionName);
		serviceMetric.setDimensionValue(dimensionValue);
		serviceMetric.setName(resourceTag);
		serviceMetric.setTotalDataPoint(metricStatisticsResult.getDatapoints().size());
		int sum = metricStatisticsResult.getDatapoints().stream().mapToInt(e -> e.getMinimum().intValue()).sum();
		serviceMetric.setSum(sum);
		logger.log(serviceMetric.toString());

	}

}
