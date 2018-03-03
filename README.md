Speech Stitcher
===============

# Todo list
*Critical*
- ETS Notification Implementation (`ETUpdateQueueWorker.java`)
- Website Copy explaining how it works
- Demo Page & API Integration

*Nice to Have*
- Allow user to get an email notification saying that their video is done, which includes the download URL for the video (Use AWS SES to send this message).
- Website Copy explaining how it works
- Improve Docs (incl. README and JDoc in Code)


## Configuration
| ENV Var     | Value                   |
| :---------- | :---------------------- |
| AWS_ACCESS  | IAM Access Key          |
| AWS_SECRET  | IAM Access Secret       |


### AWS Setup
1. Create 2 S3 buckets, one for the input sources, and one for the output files. The buckets should be in the same
region as your server, amd the ETS Pipeline that will be created in the next step to keep data transfer costs to a
minimum.  
2. Create an ETS Pipeline and create SNS Topic(s) for the desired notifications. You should be able to use the same
topic for all ET Pipeline Notifications.
3. Create an SQS Standard Queue and add it as a subscriber to the SNS Topic that was just created. Be sure to set the
Delivery Delay to 0 to ensure that the client receives the messages as soon as possible.