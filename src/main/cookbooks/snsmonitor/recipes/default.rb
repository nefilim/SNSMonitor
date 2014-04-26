#
# Cookbook Name:: snsmonitor
# Recipe:: default
#
# Copyright 2014, YOUR_COMPANY_NAME
#
# All rights reserved - Do Not Redistribute
#
#

Chef::Log.info("installing SNS Monitor")

src_filename = "snsMonitor-#{node[:snsmonitor][:version]}.tgz"
src_filepath = "#{Chef::Config['file_cache_path']}/#{src_filename}"
extract_path = "#{node[:snsmonitor][:app_dir]}"

user "#{node[:snsmonitor][:system_user]}" do
  uid 512
  system true
  action :create
end

group "#{node[:snsmonitor][:system_group]}" do
  members "#{node[:snsmonitor][:system_user]}"
  system true
  action :create
end

case node["platform"]
when "amazon"
  package "apache-commons-daemon" do
    action :install
    ignore_failure true
  end

  package "apache-commons-daemon-jsvc" do
    action :install
    ignore_failure true
  end
end

directory "/etc/#{node[:snsmonitor][:app_name]}" do
  owner node[:snsmonitor][:system_user]
  group node[:snsmonitor][:system_group]
  mode "0755"
end

directory "/var/log/#{node[:snsmonitor][:app_name]}" do
  owner node[:snsmonitor][:system_user]
  group node[:snsmonitor][:system_group]
  mode "0755"
end

cookbook_file "#{node[:snsmonitor][:config_dir]}/logback.xml" do
  source "logback.xml"
  mode 0444
  owner "#{node[:snsmonitor][:system_user]}"
  group "#{node[:snsmonitor][:system_group]}"
end

remote_file src_filepath do
  source node[:snsmonitor][:url]
  #checksum node['nginx']['foo123']['checksum']
  owner "#{node[:snsmonitor][:system_user]}"
  group "#{node[:snsmonitor][:system_group]}"
  mode 00644
  notifies :run, "bash[extract_module]", :immediately
end

bash 'extract_module' do
  cwd ::File.dirname(src_filepath)
  code <<-EOH
    mkdir -p #{extract_path}
    tar xzf #{src_filename} -C #{extract_path}
    mv #{extract_path}/*/* #{extract_path}/
    chown -R #{node[:snsmonitor][:system_user]}:#{node[:snsmonitor][:system_group]} #{extract_path}
    EOH
  action :nothing
end

template "/etc/init.d/#{node[:snsmonitor][:app_name]}" do
  source "initd.sh.erb"
  owner node[:snsmonitor][:system_user]
  group node[:snsmonitor][:system_user]
  mode "0755"
  variables(
    :service_dir => node[:snsmonitor][:app_dir],
    :service_name => node[:snsmonitor][:app_name],
    :service_user => node[:snsmonitor][:system_user],
    :service_main_class => node[:snsmonitor][:main_class],
    :service_config_dir => node[:snsmonitor][:config_dir]
  )
end

template "#{node[:snsmonitor][:config_dir]}/application.conf" do
  source "application.conf.erb"
  owner node[:snsmonitor][:system_user]
  group node[:snsmonitor][:system_user]
  mode "0444"
  variables(
    :service_dir => node[:snsmonitor][:app_dir],
    :service_name => node[:snsmonitor][:app_name],
    :service_user => node[:snsmonitor][:system_user],
    :service_main_class => node[:snsmonitor][:main_class],
    :service_config_dir => node[:snsmonitor][:config_dir]
  )
end

service "#{node[:snsmonitor][:app_name]}" do
  init_command "/etc/init.d/#{node[:snsmonitor][:app_name]}"
  supports :restart => true, :status => true, :reload => false
  action [ :enable, :start ]
end
