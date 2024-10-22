resource "aws_alb" "main" {
  name            = "kickoff-lb"
  subnets         = var.public_subnet_ids
  security_groups = var.lb_sg_ids
}

# This tells the load balancer to listen on a specific port and forward traffic to a target group
resource "aws_alb_listener" "alb_main_listener" {
  load_balancer_arn = aws_alb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "fixed-response"
    fixed_response {
      content_type = "text/plain"
      message_body = "404: Not Found"
      status_code  = "404"
    }
  }
}

# resource "aws_alb_target_group" "users_app" {
#   name        = "users-kickoff-target-group"
#   port        = 8081
#   protocol    = "HTTP"
#   vpc_id      = var.vpc_id
#   target_type = "ip"

#   health_check {
#     healthy_threshold   = "2"
#     interval            = "30"
#     protocol            = "HTTP"
#     matcher             = "200"
#     timeout             = "3"
#     path                = var.health_check_path
#     unhealthy_threshold = "2"
#   }
# }

# resource "aws_lb_listener_rule" "users_forward" {
#   listener_arn = aws_alb_listener.alb_main_listener.arn

#   action {
#     type             = "forward"
#     target_group_arn = aws_alb_target_group.users_app.arn
#   }

#   condition {
#     path_pattern {
#       values = ["/users/*"]
#     }
#   }

# }

resource "aws_alb_target_group" "app" {
  for_each = var.services

  name        = "${each.key}-target-group"
  port        = each.value.app_port
  protocol    = "HTTP"
  vpc_id      = var.vpc_id
  target_type = "ip"

  health_check {
    healthy_threshold   = "2"
    interval            = "30"
    protocol            = "HTTP"
    matcher             = "200"
    timeout             = "3"
    path                = var.health_check_path
    unhealthy_threshold = "2"
  }
}

resource "aws_lb_listener_rule" "app" {
  for_each     = var.services
  listener_arn = aws_alb_listener.alb_main_listener.arn
  priority     = 10 + index(keys(var.services), each.key) # Generate unique priority

  action {
    type             = "forward"
    target_group_arn = aws_alb_target_group.app[each.key].arn
  }

  condition {
    path_pattern {
      values = each.value.path_pattern
    }
  }
}